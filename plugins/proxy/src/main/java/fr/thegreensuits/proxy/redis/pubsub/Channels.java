package fr.thegreensuits.proxy.redis.pubsub;

import java.util.List;

public enum Channels {
    SERVERS_CREATE("orchestrator:servers:create"),
    SERVERS_SAVED("orchestrator:servers:saved"),
    SERVERS_DELETE("orchestrator:servers:delete");

    private final String channel;

    Channels(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }

    public static List<Channels> getChannels() {
        return List.of(SERVERS_CREATE, SERVERS_SAVED, SERVERS_DELETE);
    }
}
