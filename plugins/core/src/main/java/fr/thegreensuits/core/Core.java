package fr.thegreensuits.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import fr.thegreensuits.api.TheGreenSuits;
import fr.thegreensuits.api.config.RedisConfig;
import fr.thegreensuits.api.spigot.SpigotPlugin;
import fr.thegreensuits.core.listener.chat.AsyncChatListener;
import fr.thegreensuits.core.listener.player.AsyncPlayerPreLoginListener;
import fr.thegreensuits.core.listener.player.PlayerJoinListener;
import fr.thegreensuits.core.listener.player.PlayerKickListener;
import fr.thegreensuits.core.listener.player.PlayerLoginListener;
import fr.thegreensuits.core.listener.player.PlayerQuitListener;
import fr.thegreensuits.core.player.event.PlayerManagerImpl;

public class Core extends SpigotPlugin {
  private final TheGreenSuits thegreensuits;

  public Core() {
    super();

    // - Initialize TheGreenSuits
    RedisConfig redisConfig = new RedisConfig(true, "localhost", 6379, 0, "password");

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

    getLogger().info("Core disabled");
  }

  @Override
  public PlayerManagerImpl getPlayerManager() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getPlayerManager'");
  }

  private class TheGreenSuitsImpl extends TheGreenSuits {
    public TheGreenSuitsImpl(RedisConfig redisConfig) {
      super(redisConfig);
    }
  }
}
