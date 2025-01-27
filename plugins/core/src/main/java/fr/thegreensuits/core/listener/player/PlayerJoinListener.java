package fr.thegreensuits.core.listener.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import fr.thegreensuits.api.player.Player;
import fr.thegreensuits.core.player.event.PlayerManagerImpl;

public class PlayerJoinListener implements Listener {
  private final PlayerManagerImpl playerManager;

  public PlayerJoinListener(PlayerManagerImpl playerManager) {
    this.playerManager = playerManager;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerJoin(PlayerJoinEvent event) {
    event.joinMessage(null);

    Player player = this.playerManager.getPlayer(event.getPlayer().getUniqueId(), true);

    this.playerManager.onJoin(player);
  }
}
