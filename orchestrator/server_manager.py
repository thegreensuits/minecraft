from typing import List, Dict
from redis import Redis
import time
import logging
from enum import Enum
from utils import DataLayer
from docker import DockerClient
from dataclasses import dataclass

@dataclass
class ServerCreateMessage(DataLayer):
  def __init__(self, server_type: str, replicas: int = 1):
    self.server_type = server_type
    self.replicas = replicas

class ServerDeleteMessage(DataLayer):
  def __init__(self, server_id: str):
    self.server_id = server_id

class ServerUpdateMessage(DataLayer):
  def __init__(self, server_id: int, playersUniqueId: List[str]):
    self.server_id = server_id
    self.playersUniqueId = playersUniqueId

class ServerType(str, Enum):
  PROXY = "proxy"
  HUB = "hub"
  SURVIVAL = "survival"
  #VIEWING_PARTY = "viewing-party"
  #MINIGAME_CATFIGHT = "minigame_catfight"

class ServerStatus(str, Enum):
  IDLE = "idle"
  STARTING = "starting"
  RUNNING = "running"
  STOPPING = "stopping"
  STOPPED = "stopped"

class ServerTemplate:
    def __init__(self,
                 image: str,
                 type: ServerType,
                 slots: int,
                 start_port: str,
                 end_port: str,
                 min_ram: int = 512,
                 max_ram: int = 1024,
                 schedule_delay: int = 60,
                 min_replicas: int = 1,
                 max_replicas: int = 1,
                 volume: str = None):
        self.image = image
        self.type = type
        self.slots = slots
        self.ports = (start_port, end_port)
        self.ram = (min_ram, max_ram)
        self.schedule_delay = schedule_delay
        self.replicas = (min_replicas, max_replicas)
        self.volume = volume

class Player:
  def __init__(self, uuid: str, server_id: int):
    self.uuid = uuid
    self.server_id = server_id

class Server(DataLayer):
  def __init__(self, server_type: ServerType, replica: int, address: str, container_id: str, port: str = None):
    self.id = f"{server_type}:{replica}"
    self.address = address
    self.type = server_type
    self.replica = replica
    self.container_id = container_id
    self.port = port
    self.status = ServerStatus.IDLE
    self.players = []

  def add_player(self, player: Player):
    self.players.append(player)

  def remove_player(self, player: Player):
    self.players.remove(player)

class ServerManager:
  def __init__(self,
               redis: Redis,
               docker: DockerClient,
               networks,
               container_prefix: str,
               templates: Dict[ServerType, ServerTemplate],
               logger: logging.Logger = None,
               servers: Dict[ServerType, List[Server]] = None,
               redis_channels: Dict[str, str] = None):
    self.logger = logger or logging.getLogger(__name__)

    self.redis = redis
    self.docker = docker
    self.networks = networks
    self.container_prefix = container_prefix
    self.templates = templates
    self.redis_channels = redis_channels

    self.servers = servers or self.get_servers(container_prefix)

  def get_servers(self, container_prefix: str) -> Dict[ServerType, List[Server]]:
    servers: Dict[ServerType, List[Server]] = {server_type: [] for server_type in ServerType}

    servers_network = self.networks[0]

    for container in self.docker.containers.list(filters={"network": servers_network.name}):
      container_name = container.name

      if(container_name.startswith(container_prefix)):
        # - Get server infos under name format: {container_prefix}-{server_type}-{server_replica}
        server_infos = container_name.replace(container_prefix + "-", "")

        # - Get server types and replica, format is {server_type}-{server_replica} but could break for minigames (...-minigame-catfight-1)
        server_type = server_infos.split("-")[-2]
        
        if(server_type == "redis"):
          continue

        server_replica = int(server_infos.split("-")[-1])

        container_id = container.id

        container_port = next(iter(container.ports.keys())).split("/")[0]

        if not servers.get(server_type):
          servers[server_type] = []

        # - Check if the server is already in the list using container_id, if not then add it
        if not any(server.container_id == container_id for server in servers[server_type]):
          server = Server(server_type, server_replica, container_name, container_id, container_port)
          servers[server_type].append(server)

          self._save_server(server)
        else:
          server = next(server for server in servers[server_type] if server.container_id == container_id)
          servers[server_type].append(server)

    return servers
  
  def get_server(self, server_id: str) -> Server:
    server_type, server_replica = server_id.split(":")
    servers = self.servers.get(server_type)

    if not servers:
      return

    return next((server for server in servers if server.replica == server_replica), None)
  
  def create_server(self, message: ServerCreateMessage) -> Server:
    server_type = message.server_type

    template = self.get_template(server_type)
    
    if not self.servers.get(server_type):
      self.servers[server_type] = []

    # - Pull image to make sure it's up to date
    #self._pull_image(template.image)

    for _ in range(message.replicas):
      self.logger.info(f"Creating new server for {message.server_type}")

      current_replica = len(self.servers[server_type]) + 1
      
      port = self._get_available_port(template.ports)

      if not port:
        self.logger.error(f"No available ports for {server_type}")
        return

      container_name = f"{self.container_prefix}-{server_type}-{current_replica}"

      container = self.docker.containers.run(
        template.image,
        name=container_name,
        ports={f"{port}/tcp": port},
        network=self.networks[0].name,
        environment={'PORT': port, 'SERVER_TYPE': server_type},
        volumes={template.volume: {'bind': "/data", 'mode': 'rw'}} if template.volume else None,
        detach=True,
        restart_policy={"Name": "unless-stopped"}
      )

      # - Connect all networks to the container
      #for network in self.networks:
      #  container.connect(network)

      server = Server(server_type, current_replica, container_name, container.id, port)
      self.servers[server_type].append(server)

      self._save_server(server)

      self.logger.info(f"Created server {server.id} for {server.type}")

  def delete_server(self, message: ServerDeleteMessage) -> int:
    server = self.get_server(message.server_id)

    if not server:
      self.logger.error(f"Server {message.server_id} not found")
      return
    
    self.logger.info(f"Deleting server {server.id}")
    
    self.redis.delete(f"server:{server.id}")

    self.servers[server.type].remove(server)

    server_container = self.docker.containers.get(server.container_id)

    if not server_container:
      self.logger.error(f"Container {server.container_id} not found")
      return

    server_container.stop()
    server_container.remove()

    self.logger.info(f"Deleted server {server.id}")
    return

  def update_server(self, message: ServerUpdateMessage) -> Server:
    self.logger.info(f"Updating server infos {message.server_id}")

    server = self.get_server(message.server_id)

    if not server:
      self.logger.error(f"Server {message.server_id} not found")
      return

    # TODO: Update server infos

    self._save_server(server)
    pass

  def manage_servers_availability(self, server_type: ServerType):
    template = self.get_template(server_type)

    if not template:
      self.logger.error(f"Template not found for {server_type}")
      return
    
    if not self.servers.get(server_type):
      self.servers[server_type] = []

    while True:
      # - Refresh servers list
      self.servers = self.get_servers(self.container_prefix)

      running_replicas = len(self.servers[server_type])
      min_replicas, max_replicas = template.replicas

      if running_replicas < min_replicas:
        self.create_server(ServerCreateMessage(server_type, min_replicas - running_replicas))

      elif running_replicas > max_replicas:
        for _ in range(running_replicas - max_replicas):
          server = self.servers[server_type].pop()
          self.delete_server(ServerDeleteMessage(server.id))

      elif running_replicas > min_replicas:
        # - Check if any server can be deleted
        for server in self.servers[server_type]:
          if server.status == ServerStatus.IDLE:
            self.logger.info(f"Deleting idle server {server.id}")
            self.delete_server(ServerDeleteMessage(server.id))
            break

      if (server_type.startswith("minigame") and
          self._should_create_minigame_server(server_type)):
        self.create_server(ServerCreateMessage(server_type))

      time.sleep(template.schedule_delay)

  def get_template(self, server_type: ServerType) -> ServerTemplate:
    return self.templates.get(server_type)
  
  def _pull_image(self, image: str):
    self.logger.info(f"Pulling image {image}")
    self.docker.images.pull(image)

  def _get_available_port(self, ports_range: tuple[str, str]) -> str:
    """
    Get an available port from the range
    """
    start_port, end_port = ports_range

    for port in range(int(start_port), int(end_port)):
        # Check if the port is used in any container
        if not any(
            str(port) in container.ports.keys() or
            str(port) in (
                mapping.get("HostPort")
                for port_list in container.ports.values() if isinstance(port_list, list)
                for mapping in port_list if isinstance(mapping, dict)
            )
            for container in self.docker.containers.list()
        ):
            return str(port)

    return None

  def _should_create_minigame_server(self, game_type) -> bool:
    """
    Determine if a new minigame server should be created based on:
    1. Current player count vs slots
    2. Game state
    """
    # TODO: Refactor this method !

    server_status_key = f'minigame:{game_type}:status'
    server_status = self.redis.hgetall(server_status_key)

    current_players = int(server_status.get('players', 0))
    max_slots = int(server_status.get('slots', 1))
    game_state = server_status.get('state', '')

    # Create new server if:
    # 1. Current servers are almost full (80% of slots are taken)
    # 2. Game state is not "waiting"
    return (
        current_players / max_slots > 0.8 or 
        game_state != 'waiting'
    )

  def _save_server(self, server: Server):
    self.redis.set(f"server:{server.id}", server.to_json())

    # - Publish server saved event
    self.redis.publish(self.redis_channels["servers_saved"], server.to_json())
