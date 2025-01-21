package fr.thegreensuits.folia_survival;

import org.bukkit.plugin.java.JavaPlugin;

import fr.thegreensuits.folia_survival.listener.AsyncPlayerChatListener;
import fr.thegreensuits.folia_survival.listener.PlayerJoinListener;
import fr.thegreensuits.folia_survival.listener.PlayerQuitListener;

public abstract class Survival extends JavaPlugin {
    public static Survival _INSTANCE;

    public Survival() {
        _INSTANCE = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), _INSTANCE);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), _INSTANCE);
        getServer().getPluginManager().registerEvents(new AsyncPlayerChatListener(), _INSTANCE);

        getLogger().info("[Survival] plugin enabled");
    }

    @Override
    public void onDisable() {
        super.onLoad();
        getLogger().info("[Survival] plugin disabled");
    }
}
