package fr.thegreensuits.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.thegreensuits.api.TheGreenSuits;
import fr.thegreensuits.api.server.manager.ServerManager;
import fr.thegreensuits.api.server.status.ServerStatus;
import fr.thegreensuits.api.spigot.SpigotPlugin;
import fr.thegreensuits.core.listener.chat.AsyncChatListener;
import fr.thegreensuits.core.listener.player.AsyncPlayerPreLoginListener;
import fr.thegreensuits.core.listener.player.PlayerJoinListener;
import fr.thegreensuits.core.listener.player.PlayerKickListener;
import fr.thegreensuits.core.listener.player.PlayerLoginListener;
import fr.thegreensuits.core.listener.player.PlayerQuitListener;
import fr.thegreensuits.core.player.event.PlayerManagerImpl;

public class Core extends SpigotPlugin {
  private TheGreenSuits thegreensuits;
  private ServerManager serverManager;

  private final PlayerManagerImpl playerManager;

  public Core() {
    super();

    this.playerManager = new PlayerManagerImpl();
  }

  @Override()
  public void onLoad() {
    this.getLogger().info("Core loaded");
    // this.serverManager.updateServer(this.thegreensuits.getServerId(),
    // ServerStatus.STARTING, true);
  }

  @Override()
  public void onEnable() {
    saveDefaultConfig();
    PluginManager pluginManager = Bukkit.getPluginManager();

    String serverId = getConfig().getString("server.id");
    if (serverId == null || serverId.isEmpty() || serverId.equals("-1")) {
      throw new IllegalStateException("server-id is not defined in the configuration file");
    }

    // - Initialize TheGreenSuits
    this.thegreensuits = new TheGreenSuitsImpl(serverId);
    this.serverManager = this.thegreensuits.getServerManager();

    // - Register events listeners
    this.registerEvents(pluginManager);

    this.getLogger().info("Core enabled");

    this.serverManager.updateServer(this.thegreensuits.getServerId(), ServerStatus.RUNNING, true);
  }

  private void registerEvents(PluginManager pluginManager) {
    PlayerManagerImpl playerManager = this.getPlayerManager();

    // - Register Player events listeners
    pluginManager.registerEvents(new AsyncPlayerPreLoginListener(playerManager), this);
    pluginManager.registerEvents(new PlayerJoinListener(playerManager), this);
    pluginManager.registerEvents(new PlayerKickListener(playerManager), this);
    pluginManager.registerEvents(new PlayerLoginListener(playerManager), this);
    pluginManager.registerEvents(new PlayerQuitListener(playerManager), this);

    // - Register Chat events listeners
    pluginManager.registerEvents(new AsyncChatListener(), this);
  }

  @Override()
  public void onDisable() {
    this.thegreensuits.close();

    this.getLogger().info("Core disabled");

    this.serverManager.updateServer(this.thegreensuits.getServerId(), ServerStatus.STOPPED, true);
    this.serverManager.removeServer(this.thegreensuits.getServerId());
  }

  @Override
  public PlayerManagerImpl getPlayerManager() {
    return this.playerManager;
  }

  private class TheGreenSuitsImpl extends TheGreenSuits {
    public TheGreenSuitsImpl(String serverId) {
      super(serverId);
    }

    @Override
    public Logger getLogger() {
      return LoggerFactory.getLogger(Core.class);
    }

    @Override
    public void close() {
      Core.this.serverManager.updateServer(this.getServerId(), ServerStatus.STOPPING, true);

      super.close();
    }
  }
}
