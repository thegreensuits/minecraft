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
import fr.thegreensuits.api.server.manager.ServerManager;
import fr.thegreensuits.api.server.status.ServerStatus;
import fr.thegreensuits.api.utils.StaticInstance;
import fr.thegreensuits.api.utils.helper.RedisExecutor;
import fr.thegreensuits.proxy.config.ConfigManager;
import fr.thegreensuits.proxy.listener.server.InitialServerListener;
import fr.thegreensuits.proxy.redis.pubsub.listener.ServerCreatedListener;
import fr.thegreensuits.proxy.redis.pubsub.listener.ServerUpdatedListener;
import jakarta.inject.Inject;
import lombok.Getter;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Plugin(id = "proxy", name = "Proxy", version = BuildConstants.VERSION, description = BuildConstants.DESCRIPTION)
public class Proxy extends StaticInstance<Proxy> {
  // - The Green Suits
  private final TheGreenSuits thegreensuits;
  private final ServerManager serverManager;
  private final RedisExecutor redisExecutor;

  // - Velocity
  @Getter
  private final ProxyServer proxy;
  private final EventManager eventManager;

  // - Configuration
  @Getter
  private final ConfigManager configManager;

  // - Misc
  @Getter
  private final Logger logger;

  @Inject
  public Proxy(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
    super();

    this.configManager = new ConfigManager(dataDirectory);
    this.configManager.loadConfig();

    // - Initialize TheGreenSuits
    String serverId = this.configManager.getServerId();
    if (serverId.equals("-1")) {
      throw new IllegalStateException("server-id is not defined in the configuration file");
    }

    this.thegreensuits = new TheGreenSuitsImpl(serverId);
    this.serverManager = this.thegreensuits.getServerManager();
    this.redisExecutor = new RedisExecutor(this.thegreensuits.getJedisPool());

    this.proxy = proxy;
    this.eventManager = proxy.getEventManager();
    this.logger = logger;

    this.serverManager.updateServer(serverId, ServerStatus.STARTING);
  }

  @Subscribe
  public void onProxyInitialization(ProxyInitializeEvent event) {
    this.logger.info("Proxy initializing...");

    // - Register listeners
    this.registerListeners();

    // - Register current servers on proxy
    this.registerServers();

    this.logger.info("Proxy initialized");
    this.serverManager.updateServer(this.thegreensuits.getServerId(), ServerStatus.RUNNING);
  }

  private void registerListeners() {
    this.logger.info("Registering listeners...");

    this.logger.info("Registering Redis channel listeners");
    this.redisHelper.executeVoid(jedis -> {
      jedis.subscribe(new ServerCreatedListener(this), Channels.SERVERS_CREATED.getChannel());
      jedis.subscribe(new ServerUpdatedListener(this), Channels.SERVERS_UPDATED.getChannel());
      return null;
    });

    this.logger.info("Registering Velocity events listeners");
    this.eventManager.register(this, new InitialServerListener(this.proxy));

    this.logger.info("Listeners registered");
  }

  /**
   * Register all servers on the proxy
   */
  private void registerServers() {
    this.logger.info("Registering servers...");

    this.thegreensuits.getServerManager().getServers().forEach((id, server) -> {
      ServerInfo serverInfo = new ServerInfo(server.getId(), server.buildInetSocketAddress());
      this.proxy.registerServer(serverInfo);

      this.logger.info("Server {} registered", server.getId());
    });

    this.logger.info("Registered {} servers", this.thegreensuits.getServerManager().getServers().size());
  }

  @Subscribe
  public void onProxyShutdown(ProxyShutdownEvent event) {
    this.logger.info("Shutting down proxy...");
    this.thegreensuits.close();

    String serverId = this.thegreensuits.getServerId();
    this.serverManager.updateServer(serverId, ServerStatus.STOPPED);
    this.serverManager.removeServer(serverId);
    this.logger.info("Proxy shut down");
  }

  @Override
  protected int getClassId() {
    return 1;
  }

  private class TheGreenSuitsImpl extends TheGreenSuits {
    public TheGreenSuitsImpl(String serverId) {
      super(serverId);
    }

    @Override
    public Logger getLogger() {
      return LoggerFactory.getLogger(Proxy.class);
    }

    @Override
    public void close() {
      Proxy.this.serverManager.updateServer(this.getServerId(), ServerStatus.STOPPING);

      super.close();
    }
  }
}
