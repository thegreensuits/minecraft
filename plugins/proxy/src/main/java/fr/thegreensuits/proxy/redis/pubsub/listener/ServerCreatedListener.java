package fr.thegreensuits.proxy.redis.pubsub.listener;

import org.slf4j.Logger;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;

import fr.thegreensuits.api.server.Server;
import redis.clients.jedis.JedisPubSub;

public class ServerCreatedListener extends JedisPubSub {
    private final ProxyServer proxy;
    private final Logger logger;

    public ServerCreatedListener(ProxyServer proxy, Logger logger) {
        this.proxy = proxy;
        this.logger = logger;
    }

    @Override
    public void onMessage(String channel, String message) {
        Server server = Server.deserialize(message, Server.class);

        if (this.proxy.getServer(server.getId()) == null) {
            ServerInfo serverInfo = new ServerInfo(server.getId(), server.buildInetSocketAddress());
            this.proxy.registerServer(serverInfo);

            this.logger.info("Proxy server {} registered", server.getId());
            return;
        }

        this.logger.warn("Proxy server {} already registered", server.getId());
    }
}
