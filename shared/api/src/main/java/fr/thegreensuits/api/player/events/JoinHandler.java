package fr.thegreensuits.api.player.events;

import java.util.UUID;

import fr.thegreensuits.api.player.Player;

public interface JoinHandler {
  default void onPreJoin(UUID player, String username) {
  }

  default void onJoin(Player player) {
  }

  default void onQuit(Player player) {
  }
}
