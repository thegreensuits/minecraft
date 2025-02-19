package fr.thegreensuits.proxy;

import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;

import fr.thegreensuits.api.TheGreenSuits;
import fr.thegreensuits.api.redis.pubsub.Channels;
import fr.thegreensuits.api.server.status.ServerStatus;
import fr.thegreensuits.api.utils.StaticInstance;
import fr.thegreensuits.proxy.config.ConfigManager;
import fr.thegreensuits.proxy.listener.server.InitialServerListener;
import fr.thegreensuits.proxy.redis.pubsub.listener.ServerCreatedListener;
import fr.thegreensuits.proxy.redis.pubsub.listener.ServerUpdatedListener;
import jakarta.inject.Inject;
import lombok.Getter;
import redis.clients.jedis.Jedis;

import java.nio.file.Path;

import org.slf4j.Logger;

@Plugin(id = "proxy", name = "Proxy", version = BuildConstants.VERSION, description = BuildConstants.DESCRIPTION)
public class Proxy extends StaticInstance<Proxy> {
    private final TheGreenSuits thegreensuits;

    @Getter
    private final ProxyServer proxy;
    @Getter
    private final ConfigManager configManager;
    private final EventManager eventManager;

    @Getter
    private final Logger logger;

    @Inject
    public Proxy(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        super();

        this.configManager = new ConfigManager(dataDirectory);
        this.configManager.loadConfig();

        this.proxy = proxy;
        this.logger = logger;
        this.eventManager = proxy.getEventManager();

        // - Initialize TheGreenSuits
        String serverId = this.configManager.getServerId();
        if (serverId.equals("-1")) {
            throw new IllegalStateException("server-id is not defined in the configuration file");
        }

        this.thegreensuits = new TheGreenSuitsImpl(serverId);
    }

    @Override
    protected int getClassId() {
        return 1;
    }

    public void registerListeners() {
        Jedis jedis = TheGreenSuits.get().getJedisPool().getResource();

        // - Register Jedis channel events listeners
        jedis.subscribe(new ServerCreatedListener(this.proxy, this.logger), Channels.SERVERS_CREATED.getChannel());
        jedis.subscribe(new ServerUpdatedListener(this.proxy, this.logger), Channels.SERVERS_UPDATED.getChannel());
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.thegreensuits.getServerManager().updateServer(this.thegreensuits.getServerId(), ServerStatus.STARTING);

        // - Register Velocity events listeners
        this.eventManager.register(this, new InitialServerListener(this.proxy));

        // - Register current servers on proxy
        this.thegreensuits.getServerManager().getServers().forEach((id, server) -> {
            ServerInfo serverInfo = new ServerInfo(server.getId(), server.buildInetSocketAddress());
            this.proxy.registerServer(serverInfo);

            this.logger.info("Server {} registered", server.getId());
        });

        this.logger.info("Proxy initialized");
        this.thegreensuits.getServerManager().updateServer(this.thegreensuits.getServerId(), ServerStatus.RUNNING);
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.thegreensuits.close();

        this.logger.info("Proxy shutdown");

        this.thegreensuits.getServerManager().updateServer(this.thegreensuits.getServerId(), ServerStatus.STOPPED);
        this.thegreensuits.getServerManager().removeServer(this.thegreensuits.getServerId());
    }

    private class TheGreenSuitsImpl extends TheGreenSuits {
        public TheGreenSuitsImpl(String serverId) {
            super(serverId);
        }

        @Override
        public Logger getLogger() {
            return Proxy.this.getLogger();
        }

        @Override
        public void close() {
            this.getServerManager().updateServer(this.getServerId(), ServerStatus.STOPPING);

            super.close();
        }
    }
}
