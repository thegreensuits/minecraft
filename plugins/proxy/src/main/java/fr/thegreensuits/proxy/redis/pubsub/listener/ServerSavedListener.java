package fr.thegreensuits.proxy.redis.pubsub.listener;

import javax.inject.Inject;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;

import fr.thegreensuits.api.TheGreenSuits;
import fr.thegreensuits.api.server.Server;
import fr.thegreensuits.proxy.redis.pubsub.Channels;
import redis.clients.jedis.JedisPubSub;

public class ServerSavedListener extends JedisPubSub {
    private final ProxyServer proxy;

    @Inject
    public ServerSavedListener(ProxyServer proxy) {
        this.proxy = proxy;
    }

    @Override
    public void onMessage(String channel, String message) {
        if (channel == Channels.SERVERS_SAVED.getChannel()) {
            Server server = Server.deserialize(message, Server.class);

            if (this.proxy.getServer(server.getId()) == null) {
                ServerInfo serverInfo = new ServerInfo(server.getId(), server.buildInetSocketAddress());
                TheGreenSuits.get().getServerManager().addServer(server);
                this.proxy.registerServer(serverInfo);
            } else {
                // TODO: Implement this
            }
        }
    }
}
