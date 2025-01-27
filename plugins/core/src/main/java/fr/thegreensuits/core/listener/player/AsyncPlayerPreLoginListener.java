package fr.thegreensuits.core.listener.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import fr.thegreensuits.core.player.event.PlayerManagerImpl;

public class AsyncPlayerPreLoginListener implements Listener {
  private final PlayerManagerImpl playerManager;

  public AsyncPlayerPreLoginListener(PlayerManagerImpl playerManager) {
    this.playerManager = playerManager;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
    this.playerManager.onPreJoin(event);
  }
}
