package fr.thegreensuits.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import fr.thegreensuits.api.config.RedisConfig;
import fr.thegreensuits.core.listener.chat.AsyncChatListener;
import fr.thegreensuits.core.listener.player.AsyncPlayerPreLoginListener;
import fr.thegreensuits.core.listener.player.PlayerJoinListener;
import fr.thegreensuits.core.listener.player.PlayerKickListener;
import fr.thegreensuits.core.listener.player.PlayerLoginListener;
import fr.thegreensuits.core.listener.player.PlayerQuitListener;
import fr.thegreensuits.core.player.event.PlayerManagerImpl;

public class Core extends JavaPlugin {
  private final TheGreenSuitsImpl thegreensuits;

  public Core() {
    super();

    RedisConfig redisConfig = new RedisConfig(isEnabled(), getName(), 0, 0, getName());

    this.thegreensuits = new TheGreenSuitsImpl(redisConfig);
  }

  @Override()
  public void onLoad() {
    getLogger().info("Core loaded");
  }

  @Override()
  public void onEnable() {
    PluginManager pluginManager = Bukkit.getPluginManager();

    // - Register events listeners
    this.registerEvents(pluginManager);

    getLogger().info("Core enabled");
  }

  private void registerEvents(PluginManager pluginManager) {
    PlayerManagerImpl playerManager = this.thegreensuits.playerManager;

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
    thegreensuits.close();

    getLogger().info("Core disabled");
  }
}
