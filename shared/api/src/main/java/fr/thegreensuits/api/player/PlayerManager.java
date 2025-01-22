package fr.thegreensuits.api.player;

import java.util.UUID;

import fr.thegreensuits.api.player.events.JoinHandler;
import fr.thegreensuits.api.utils.commons.Priority;

public interface PlayerManager {
  Player getPlayer(UUID player);

  Player getPlayer(UUID player, boolean create);

  Player forceGetPlayer(UUID player);

  void registerJoinHandler(JoinHandler handler, Priority priority);

  void connect(UUID player, String server);

  void kick(UUID player, String reason);
}
