import time
import docker
import redis
import os
import threading
import logging
from dotenv import load_dotenv
from typing import List
from server_manager import ServerManager, ServerCreateMessage, ServerDeleteMessage, ServerUpdateMessage, ServerType, ServerTemplate

load_dotenv()

class Orchestrator:
  def __init__(self,
               docker_socket=os.environ.get("DOCKER_SOCKET", "unix://var/run/docker.sock"),
               docker_api_version=os.environ.get("DOCKER_API_VERSION", "1.40"),
               docker_container_prefix=os.environ.get("DOCKER_CONTAINER_PREFIX", "orchestrator"),
               docker_network_main=os.environ.get("DOCKER_NETWORK_MAIN", "orchestrator_main"),
               redis_host=os.environ.get("REDIS_HOST", "localhost"),
               redis_port=os.environ.get("REDIS_PORT", 6379),
               redis_db=os.environ.get("REDIS_DB", 0),
               redis_password=os.environ.get("REDIS_PASSWORD", None),
               redis_channel_prefix=os.environ.get("REDIS_CHANNEL_PREFIX", "orchestrator")):
    logging.basicConfig(level=logging.INFO)
    self.logger = logging.getLogger(__name__)

    self.redis = redis.StrictRedis(host=redis_host,
                                    port=redis_port,
                                    db=redis_db,
                                    password=redis_password)
    self.redis_channels = {
      "servers_create": f"{redis_channel_prefix}:servers:create",
      "servers_saved": f"{redis_channel_prefix}:servers:saved",
      "servers_delete": f"{redis_channel_prefix}:servers:delete",
    }
    
    self.docker = docker.DockerClient(base_url=docker_socket, version=docker_api_version)
    docker_network = self.get_or_create_docker_network(docker_network_main)

    survival_volume = self.get_or_create_docker_volume(os.environ.get("DOCKER_VOLUME_SURVIVAL", "survival"))

    # - Servers management
    templates = {
      ServerType.PROXY: ServerTemplate(os.environ.get("SERVER_TEMPLATE_PROXY_IMAGE", "minecraft-proxy:latest"),
                                       ServerType.PROXY,
                                       os.environ.get("SERVER_TEMPLATE_PROXY_MAX_PLAYERS", 50),
                                       os.environ.get("SERVER_TEMPLATE_PROXY_START_PORT", "25565"),
                                       os.environ.get("SERVER_TEMPLATE_PROXY_END_PORT", "25600"),
                                       schedule_delay=os.environ.get("SERVER_TEMPLATE_PROXY_SCHEDULE_DELAY", 30)),
      ServerType.HUB: ServerTemplate(os.environ.get("SERVER_TEMPLATE_HUB_IMAGE", "minecraft-hub:latest"),
                                      ServerType.HUB,
                                      os.environ.get("SERVER_TEMPLATE_HUB_MAX_PLAYERS", 50),
                                      os.environ.get("SERVER_TEMPLATE_HUB_START_PORT", "25001"),
                                      os.environ.get("SERVER_TEMPLATE_HUB_END_PORT", "25050"),
                                      os.environ.get("SERVER_TEMPLATE_HUB_MIN_RAM", 0.5 * 1024),
                                      os.environ.get("SERVER_TEMPLATE_HUB_MAX_RAM", 4 * 1024),
                                      schedule_delay=os.environ.get("SERVER_TEMPLATE_HUB_SCHEDULE_DELAY", 10)),
      ServerType.SURVIVAL: ServerTemplate(os.environ.get("SERVER_TEMPLATE_SURVIVAL_IMAGE", "minecraft-survival:latest"),
                                          ServerType.SURVIVAL,
                                          os.environ.get("SERVER_TEMPLATE_SURVIVAL_MAX_PLAYERS", 50),
                                          os.environ.get("SERVER_TEMPLATE_SURVIVAL_START_PORT", "25051"),
                                          os.environ.get("SERVER_TEMPLATE_SURVIVAL_END_PORT", "25100"),
                                          os.environ.get("SERVER_TEMPLATE_SURVIVAL_MIN_RAM", 1 * 1024),
                                          os.environ.get("SERVER_TEMPLATE_SURVIVAL_MAX_RAM", 8 * 1024),
                                          min_replicas=0,
                                          schedule_delay=os.environ.get("SERVER_TEMPLATE_SURVIVAL_SCHEDULE_DELAY", 30),
                                          volume=survival_volume.name),
    }

    self.server_manager = ServerManager(self.redis, self.docker, docker_network, docker_container_prefix, templates, logger=self.logger, redis_channels=self.redis_channels)

  def start(self):
    self.logger.info("Starting orchestrator...")

    threads: List[threading.Thread] = []

    for server_type in ServerType:
      t = threading.Thread(target=self.server_manager.manage_servers_availability, args=(server_type,), daemon=True)
      threads.append(t)

    for channel in self.redis_channels.values():
      t = threading.Thread(target=self._listen_redis_channel, args=(channel,), daemon=True)
      threads.append(t)

    for t in threads:
      self.logger.info(f"Starting thread {t.name}")
      t.start()

    self.logger.info("Orchestrator started!")

    while True:
      time.sleep(60)

  def stop(self):
    self.logger.info("Stopping orchestrator...")
    os._exit(0)

  def get_or_create_docker_volume(self, volume_name):
    if not any(v.name == volume_name for v in self.docker.volumes.list()):
      return self.docker.volumes.create(volume_name)
    else:
      return self.docker.volumes.get(volume_name)

  def get_or_create_docker_network(self, network_name):
    if not any(n.name == network_name for n in self.docker.networks.list()):
      return self.docker.networks.create(network_name)
    else:
      return self.docker.networks.get(network_name)

  def _listen_redis_channel(self, channel):
    pubsub = self.redis.pubsub()
    pubsub.subscribe(channel)

    for message in pubsub.listen():
      if message['type'] == 'message':
        if not message['data'] or message['data'] == b'':
            self.logger.error("Received empty message from Redis")
            continue

        elif channel == self.redis_channels['servers_delete']:
          self.server_manager.deleteServer(ServerDeleteMessage.from_json(message['data']))

def main():
  orchestrator = Orchestrator()
  
  try:
    if orchestrator.start():
      print("Orchestrator started")

  except Exception as e:
        print(f"Error: {e}")
        
  finally:
    orchestrator.stop()

if __name__ == '__main__':
  main()