package fr.thegreensuits.proxy.listener.server;

import java.util.Optional;

import javax.inject.Inject;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.ProxyServer;

import fr.thegreensuits.api.TheGreenSuits;
import fr.thegreensuits.api.server.status.ServerStatus;
import fr.thegreensuits.api.server.type.ServerType;

public class InitialServerListener {
    private final ProxyServer proxy;
    private final TheGreenSuits thegreensuits;

    @Inject
    public InitialServerListener(ProxyServer proxy) {
        this.proxy = proxy;
        this.thegreensuits = TheGreenSuits.get();
    }

    @Subscribe
    public void onPlayerChooseServer(PlayerChooseInitialServerEvent event) {
        System.out.println(this.thegreensuits.getServerManager().getServers().keySet());

        this.thegreensuits.getServerManager().getServers().values().stream()
                .filter(server -> server.getType().equals(ServerType.HUB)
                        && server.getStatus().equals(ServerStatus.RUNNING))
                .map(server -> this.proxy.getServer(server.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .ifPresent(event::setInitialServer);
    }
}
