package fr.thegreensuits.api.redis.pubsub;

public enum Channels {
  SERVERS_CREATED("orchestrator:servers:created"),
  SERVERS_UPDATED("orchestrator:servers:updated");

  private final String channel;

  Channels(String channel) {
    this.channel = channel;
  }

  public String getChannel() {
    return channel;
  }
}
