package fr.thegreensuits.api.spigot;

import org.bukkit.plugin.java.JavaPlugin;

import fr.thegreensuits.api.player.PlayerManager;

public abstract class SpigotPlugin extends JavaPlugin {
    public abstract PlayerManager getPlayerManager();
}
