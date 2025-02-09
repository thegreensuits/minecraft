package fr.thegreensuits.proxy;

import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;

import fr.thegreensuits.api.TheGreenSuits;
import fr.thegreensuits.api.config.RedisConfig;
import fr.thegreensuits.proxy.listener.server.InitialServerListener;
import fr.thegreensuits.proxy.redis.pubsub.Channels;
import fr.thegreensuits.proxy.redis.pubsub.listener.ServerSavedListener;
import redis.clients.jedis.Jedis;

import javax.inject.Inject;

import org.slf4j.Logger;

@Plugin(id = "proxy", name = "proxy", version = BuildConstants.VERSION, description = BuildConstants.DESCRIPTION)
public class Proxy {
    private final TheGreenSuits thegreensuits;

    private final ProxyServer proxy;
    private final EventManager eventManager;

    private final Logger logger;

    @Inject
    public Proxy(ProxyServer proxy, EventManager eventManager, Logger logger, ServerSavedListener serverSavedListener) {
        this.proxy = proxy;
        this.eventManager = eventManager;

        this.logger = logger;

        // - Initialize TheGreenSuits
        RedisConfig redisConfig = new RedisConfig(true, "127.0.0.1", 6379, 0, "");
        this.thegreensuits = new TheGreenSuitsImpl(redisConfig);
    }

    @Inject
    public void registerListeners(InitialServerListener initialServerListener) {
        Jedis jedis = TheGreenSuits.get().getJedisPool().getResource();

        // - Register Velocity events listeners
        eventManager.register(this, initialServerListener);

        // - Register Jedis channel events listeners
        jedis.subscribe(new ServerSavedListener(this.proxy), Channels.SERVERS_SAVED.getChannel());
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        System.out.println(this.thegreensuits.getServerManager().getServers().keySet());

        // - Register current servers on proxy
        this.thegreensuits.getServerManager().getServers().forEach((id, server) -> {
            ServerInfo serverInfo = new ServerInfo(server.getId(), server.buildInetSocketAddress());
            this.proxy.registerServer(serverInfo);
        });

        this.logger.info("Proxy initialized");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.thegreensuits.close();

        this.logger.info("Proxy shutdown");
    }

    private class TheGreenSuitsImpl extends TheGreenSuits {
        public TheGreenSuitsImpl(RedisConfig redisConfig) {
            super(redisConfig);
        }
    }
}
