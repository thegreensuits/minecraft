package fr.thegreensuits.survival.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import fr.thegreensuits.api.TheGreenSuits;
import fr.thegreensuits.api.player.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PlayerJoinListener implements Listener {
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerJoin(PlayerJoinEvent event) {
    /*
     * Player player =
     * TheGreenSuits.spigot().getPlayerManager().retrieve(event.getPlayer().
     * getUniqueId());
     * Component playerDisplayName = Component.text(player.getDisplayName());
     * 
     * Component playerDisplayName = Component.text(event.getPlayer().getName(),
     * NamedTextColor.GRAY);
     * 
     * Component message = Component.text("[", NamedTextColor.DARK_GRAY)
     * .append(Component.text("+", NamedTextColor.AQUA))
     * .append(Component.text("] ", NamedTextColor.DARK_GRAY))
     * .append(Component.text(event.getPlayer().getName(), NamedTextColor.AQUA))
     * .append(Component.text(" a rejoint le serveur !", NamedTextColor.GRAY));
     * event.joinMessage(message);
     * 
     * event.getPlayer().displayName(playerDisplayName);
     * event.getPlayer().playerListName(playerDisplayName);
     */
  }
}
