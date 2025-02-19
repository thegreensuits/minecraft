package fr.thegreensuits.api.redis.pubsub.listener;

import org.slf4j.Logger;

import fr.thegreensuits.api.TheGreenSuits;
import fr.thegreensuits.api.server.Server;
import fr.thegreensuits.api.server.manager.ServerManager;
import redis.clients.jedis.JedisPubSub;

public class ServerCreatedListener extends JedisPubSub {
    private final Logger logger = TheGreenSuits.get().getLogger();
    private final ServerManager serverManager;

    public ServerCreatedListener() {
        super();

        this.serverManager = TheGreenSuits.get().getServerManager();
    }

    @Override
    public void onMessage(String channel, String message) {
        Server server = Server.deserialize(message, Server.class);

        if (!this.serverManager.hasServer(server.getId())) {
            TheGreenSuits.get().getServerManager().addServer(server);

            this.logger.info("Server {} added to server manager", server.getId());
            return;
        } else {
            this.logger.warn("Server {} is already registered in server manager, updating it", server.getId());

            this.serverManager.updateServer(server, true);
        }
    }
}
