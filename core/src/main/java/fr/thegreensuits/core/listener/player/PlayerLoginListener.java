package fr.thegreensuits.core.listener.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import fr.thegreensuits.api.player.Player;
import fr.thegreensuits.core.player.event.PlayerManagerImpl;

public class PlayerLoginListener implements Listener {
  private final PlayerManagerImpl playerManager;

  public PlayerLoginListener(PlayerManagerImpl playerManager) {
    this.playerManager = playerManager;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerLogin(PlayerLoginEvent event) {
    Player player = this.playerManager.getPlayer(event.getPlayer().getUniqueId(), true);

    // TODO: Apply nickname if exists, and apply permissions
    // https://gitlab.com/BerryGames/BerryGamesCore/-/blob/master/src/main/java/net/berrygames/core/listeners/general/GeneralConnectionListener.java?ref_type=heads#L65
  }
}
