package fr.thegreensuits.api.spigot;

import org.bukkit.plugin.java.JavaPlugin;

import fr.thegreensuits.api.player.PlayerManager;

public abstract class SpigotPlugin extends JavaPlugin {
    private static SpigotPlugin _INSTANCE;

    public SpigotPlugin() {
        _INSTANCE = this;
    }

    public abstract PlayerManager getPlayerManager();

    public static SpigotPlugin get() {
        return _INSTANCE;
    }
}
