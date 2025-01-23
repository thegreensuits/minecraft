import time
import json
import docker
import redis
import threading
import logging
from typing import Dict, Optional

class MinecraftServerOrchestrator:
    def __init__(self, 
                 redis_host='localhost', 
                 redis_port=6379, 
                 docker_socket='unix://var/run/docker.sock'):
        logging.basicConfig(level=logging.INFO)
        self.logger = logging.getLogger(__name__)

        # TODO: Get env variables like forwarding secret (send it in env vars of containers), redis host, port, docker socket, etc...

        self.servers_prefix = 'minesuits'

        self.redis = redis.Redis(host=redis_host, port=redis_port, decode_responses=True)
        
        self.docker_client = docker.DockerClient(base_url=docker_socket)

        self.docker_network = 'minesuits-network'
        self._get_or_create_docker_network()

        self.servers: Dict[str, Dict] = {
            'proxy': None,
            'hub': None,
            'survival': None,
            'viewing_party': None,
            'minigames': {}
        }
        self._inject_servers()

        self.templates = {
            'proxy': {
                'image': 'minecraft-proxy:latest',
                'ports': (25565, 25565)
            },
            'hub': {
                'image': 'minecraft-hub:latest',
                'ports': (25001, 25001)
            },
            'survival': {
                'image': 'minecraft-survival:latest',
                'ports': (25002, 25002)
            },
            'viewing_party': {
                'image': 'minecraft-viewing-party:latest',
                'ports': (25003, 25003)
            }
            #'minigames': {
            #    'slender': {
            #        'image': 'minecraft-slender:latest',
            #        'ports': (25011, 25020)
            #    },
            #    'catfight': {
            #        'image': 'minecraft-catfight:latest',
            #        'ports': (25021, 25030)
            #    }
            #}
        }

    def _get_or_create_docker_network(self):
        if not any(n.name == self.docker_network for n in self.docker_client.networks.list()):
            self.docker_network = self.docker_client.networks.create(self.docker_network)
        else:
            self.docker_network = self.docker_client.networks.get(self.docker_network)

    def _inject_servers(self):
        for container in self.docker_client.containers.list():
            container_name = container.name

            if container_name.startswith(self.servers_prefix):
                container_type = container_name.split('-')[1]

                if container_type == 'minigame':
                    game_type = container_name.split('-')[2]
                    if game_type not in self.servers['minigames']:
                        self.servers['minigames'][game_type] = []
                    self.servers['minigames'][game_type].append(container.id)

                else:
                    self.servers[container_type] = container.id

    def start_orchestration(self):
        threads = [
            threading.Thread(target=self._manage_proxy, daemon=True),
            threading.Thread(target=self._manage_hub, daemon=True),
            threading.Thread(target=self._listen_requests, daemon=True)
        ]
        
        for thread in threads:
            thread.start()

        while True:
            time.sleep(60)

    def _manage_proxy(self):
        if not self.templates.get('proxy'):
            self.logger.warning("No proxy defined")
            return

        while True:
            if not self._is_container_running('proxy'):
                self._create_server('proxy')
            time.sleep(30)
        
    def _manage_hub(self):
        if not self.templates.get('hub'):
            self.logger.warning("No hub defined")
            return

        while True:
            if not self._is_container_running('hub'):
                self._create_server('hub')
            time.sleep(5)

    def _manage_minigames(self):
        if not self.templates.get('minigames'):
            self.logger.warning("No minigames defined")
            return

        while True:
            for game_type in self.templates['minigames']:
                if self._should_create_minigame_server(game_type):
                    self._create_server('minigame', game_type)
            time.sleep(5)

    def _listen_requests(self):
        pubsub = self.redis.pubsub()
        pubsub.subscribe('minecraft:orchestrator')

        for message in pubsub.listen():
            if message['type'] == 'message':
                self._handle_request(message['data'])

    def _handle_request(self, raw_data):
        try:
            data = json.loads(raw_data)
            game_type = data.get('game_type')
            
            if game_type not in self.templates['minigames']:
                self.logger.warning(f"Unknown game type: {game_type}")
                return

            if self._should_create_minigame_server(game_type):
                self._create_server('minigame', game_type)

        except json.JSONDecodeError:
            self.logger.error(f"Invalid message format: {raw_data}")

    def _should_create_minigame_server(self, game_type) -> bool:
        """
        Determine if a new minigame server should be created based on:
        1. Current player count vs slots
        2. Game state
        """
        game_servers = self.servers['minigames'].get(game_type, [])
        
        server_status_key = f'minigame:{game_type}:status'
        server_status = self.redis.hgetall(server_status_key)

        current_players = int(server_status.get('players', 0))
        max_slots = int(server_status.get('slots', 1))
        game_state = server_status.get('state', '')

        # Create new server if:
        # 1. Current servers are almost full
        # 2. Game state is not "waiting"
        # 3. No available servers
        return (
            current_players / max_slots > 0.8 or 
            game_state != 'waiting' or 
            len(game_servers) == 0
        )

    def _create_server(self, server_type: str, game_type: Optional[str] = None):
        if not self._should_create_minigame_server(game_type):
            return

        try:
            template = self.templates[server_type]
            port = self._choose_available_port(template['ports'][0], template['ports'][1])

            if server_type == 'minigame' and game_type:
                container = self.docker_client.containers.run(
                    self.templates['minigames'][game_type],
                    ports={f'{template["ports"][0]}/tcp': template["ports"][1]},
                    network=self.docker_network.name,
                    detach=True,
                    name=f'{self.servers_prefix}-{game_type}-{len(self.servers["minigames"].get(game_type, []))}',
                    environment={'PORT': port, 'GAME_TYPE': game_type}
                )
                
                if game_type not in self.servers['minigames']:
                    self.servers['minigames'][game_type] = []
                
                self.servers['minigames'][game_type].append(container.id)
            
            elif server_type:
                container = self.docker_client.containers.run(
                    template['image'],
                    ports={f'{template["ports"][0]}/tcp': template["ports"][1]},
                    network=self.docker_network.name,
                    detach=True,
                    name=f'{self.servers_prefix}-{server_type}',
                    environment={'PORT': port}
                )
                self.servers[server_type] = container.id

            # TODO: Send rcon command to announce a new server on the network (network host and port)

            self.logger.info(f"Created {server_type} server" + (f" for {game_type}" if game_type else "") + f" on port {port}")

        except Exception as e:
            self.logger.error(f"Error creating {server_type} server: {e}")

    def _choose_available_port(self, start_port: int, end_port: int) -> int:
        for port in range(start_port, end_port):
            if not self._is_port_in_use(port):
                return port
        return None
    
    def _is_port_in_use(self, port: int) -> bool:
        for container in self.docker_client.containers.list():
            for container_port in container.attrs['NetworkSettings']['Ports']:
                if container_port == f'{port}/tcp':
                    return True
        return False

    def _is_container_running(self, server_type: str) -> bool:
        try:
            server_id = self.servers.get(server_type)
            if not server_id:
                return False
            
            container = self.docker_client.containers.get(server_id)
            return container.status == 'running'
        except docker.errors.NotFound:
            return False
        except Exception as e:
            self.logger.error(f"Error checking {server_type} container: {e}")
            return False

def main():
    orchestrator = MinecraftServerOrchestrator()
    orchestrator.start_orchestration()

if __name__ == '__main__':
    main()