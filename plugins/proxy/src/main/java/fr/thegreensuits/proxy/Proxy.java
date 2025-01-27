package fr.thegreensuits.proxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import fr.thegreensuits.api.TheGreenSuits;
import org.slf4j.Logger;

@Plugin(
    id = "proxy",
    name = "proxy",
    version = BuildConstants.VERSION
)
public class Proxy {

    @Inject private Logger logger;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        //TheGreenSuits.get().proxy()
    }
}
