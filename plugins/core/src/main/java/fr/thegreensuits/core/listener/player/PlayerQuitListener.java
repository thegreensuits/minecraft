package fr.thegreensuits.core.listener.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.thegreensuits.api.player.Player;
import fr.thegreensuits.core.player.event.PlayerManagerImpl;

public class PlayerQuitListener implements Listener {
  private final PlayerManagerImpl playerManager;

  public PlayerQuitListener(PlayerManagerImpl playerManager) {
    this.playerManager = playerManager;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerQuit(PlayerQuitEvent event) {
    event.quitMessage(null);

    Player player = this.playerManager.getPlayer(event.getPlayer().getUniqueId());

    this.playerManager.onQuit(player);
  }
}
