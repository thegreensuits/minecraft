package fr.thegreensuits.api.redis.pubsub.listener;

import org.slf4j.Logger;

import fr.thegreensuits.api.TheGreenSuits;
import fr.thegreensuits.api.server.Server;
import fr.thegreensuits.api.server.manager.ServerManager;
import redis.clients.jedis.JedisPubSub;

public class ServerUpdatedListener extends JedisPubSub {
    private final Logger logger = TheGreenSuits.get().getLogger();
    private final ServerManager serverManager;

    public ServerUpdatedListener() {
        super();

        this.serverManager = TheGreenSuits.get().getServerManager();
    }

    @Override
    public void onMessage(String channel, String message) {
        Server server = Server.deserialize(message, Server.class);

        if (this.serverManager.hasServer(server.getId())) {
            this.logger.warn("Server {} updated", server.getId());

            this.serverManager.updateServer(server);
        } else {
            this.logger.warn("Server {} is not registered in server manager, adding it", server.getId());

            this.serverManager.addServer(server);

            this.logger.info("Server {} added to server manager", server.getId());
        }
    }
}
