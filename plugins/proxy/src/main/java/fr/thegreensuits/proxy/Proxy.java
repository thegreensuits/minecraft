package fr.thegreensuits.proxy;

import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;

import fr.thegreensuits.api.TheGreenSuits;
import fr.thegreensuits.api.server.status.ServerStatus;
import fr.thegreensuits.api.server.type.ServerType;
import fr.thegreensuits.api.utils.StaticInstance;
import fr.thegreensuits.proxy.listener.server.InitialServerListener;
import fr.thegreensuits.proxy.redis.pubsub.Channels;
import fr.thegreensuits.proxy.redis.pubsub.listener.ServerSavedListener;
import jakarta.inject.Inject;
import lombok.Getter;
import redis.clients.jedis.Jedis;

import java.util.Optional;

import org.slf4j.Logger;

@Plugin(id = "proxy", name = "Proxy", version = BuildConstants.VERSION, description = BuildConstants.DESCRIPTION)
public class Proxy extends StaticInstance<Proxy> {
    private final TheGreenSuits thegreensuits;

    @Getter
    private final ProxyServer proxy;
    private final EventManager eventManager;

    @Getter
    private final Logger logger;

    @Inject
    public Proxy(ProxyServer proxy, Logger logger) {
        super();

        this.proxy = proxy;
        this.logger = logger;
        this.eventManager = proxy.getEventManager();

        // - Initialize TheGreenSuits
        this.thegreensuits = new TheGreenSuitsImpl();
    }

    @Override
    protected int getClassId() {
        return 1;
    }

    public void registerListeners() {
        Jedis jedis = TheGreenSuits.get().getJedisPool().getResource();

        // - Register Velocity events listeners
        this.eventManager.register(this, new InitialServerListener(this.proxy));

        // - Register Jedis channel events listeners
        jedis.subscribe(new ServerSavedListener(this.proxy, this.logger), Channels.SERVERS_SAVED.getChannel());
    }

    @Subscribe
    public void onPlayerChooseServer(PlayerChooseInitialServerEvent event) {
        System.out.println("@player choose " + this.thegreensuits.getServerManager().getServers().values());

        this.thegreensuits.getServerManager().getServers().values().stream()
                .filter(server -> server.getType().equals(ServerType.HUB)
                        && server.getStatus().equals(ServerStatus.RUNNING))
                .map(server -> this.proxy.getServer(server.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .ifPresent(event::setInitialServer);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // - Register current servers on proxy
        this.thegreensuits.getServerManager().getServers().forEach((id, server) -> {
            ServerInfo serverInfo = new ServerInfo(server.getId(), server.buildInetSocketAddress());
            this.proxy.registerServer(serverInfo);

            this.logger.info("Server {} registered", server.getId());
        });

        this.logger.info("Proxy initialized");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.thegreensuits.close();

        this.logger.info("Proxy shutdown");
    }

    private class TheGreenSuitsImpl extends TheGreenSuits {
    }
}
