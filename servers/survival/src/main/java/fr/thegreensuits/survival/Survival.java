package fr.thegreensuits.survival;

import org.bukkit.plugin.java.JavaPlugin;

import fr.thegreensuits.survival.handler.SurvivalJoinHandler;

import fr.thegreensuits.api.TheGreenSuits;
import fr.thegreensuits.api.utils.commons.Priority;
import fr.thegreensuits.survival.listener.AsyncChatListener;
import fr.thegreensuits.survival.listener.PlayerJoinListener;
import fr.thegreensuits.survival.listener.PlayerQuitListener;

public class Survival extends JavaPlugin {
  public static Survival _INSTANCE;

  public Survival() {
    _INSTANCE = this;
  }

  @Override()
  public void onLoad() {
    super.onLoad();
  }

  @Override()
  public void onEnable() {
    super.onEnable();

    TheGreenSuits.get().getPlayerManager().registerJoinHandler(new SurvivalJoinHandler(), Priority.HIGHEST);

    getLogger().info("Plugin enabled");
  }

  @Override()
  public void onDisable() {
    super.onLoad();
    getLogger().info("Plugin disabled");
  }
}
