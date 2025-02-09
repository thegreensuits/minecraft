package fr.thegreensuits.core.player.event;

import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import fr.thegreensuits.api.player.Player;
import fr.thegreensuits.api.player.PlayerManager;
import fr.thegreensuits.api.player.events.JoinHandler;
import fr.thegreensuits.api.utils.commons.Priority;
import fr.thegreensuits.core.player.PlayerImpl;

public class PlayerManagerImpl implements PlayerManager {
  protected final TreeMap<Priority, JoinHandler> joinHandlers;
  protected final ConcurrentHashMap<UUID, PlayerImpl> playersCache;

  public PlayerManagerImpl() {
    this.joinHandlers = new TreeMap<>();
    this.playersCache = new ConcurrentHashMap<>();
  }

  @Override
  public Player getPlayer(UUID player) {
    if (this.playersCache.containsKey(player))
      return this.playersCache.get(player);

    return null;
  }

  @Override
  public Player getPlayer(UUID player, boolean create) {
    if (this.playersCache.containsKey(player))
      return this.playersCache.get(player);

    if (create) {
      this.loadPlayer(player);
      return this.playersCache.get(player);
    }

    return null;
  }

  @Override
  public Player forceGetPlayer(UUID player) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'forceGetPlayer'");
  }

  @Override
  public void registerJoinHandler(JoinHandler handler, Priority priority) {
    this.joinHandlers.put(priority, handler);
  }

  @Override
  public void connect(UUID player, String server) {
    // TODO: Send message to proxy
    throw new UnsupportedOperationException("Unimplemented method 'connect'");
  }

  @Override
  public void kick(UUID player, String reason) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'kick'");
  }

  public void onPreJoin(AsyncPlayerPreLoginEvent event) {
    for (JoinHandler handler : this.joinHandlers.values())
      handler.onPreJoin(event.getUniqueId(), event.getName());

    this.loadPlayer(event.getUniqueId());
  }

  public void onJoin(Player player) {
    for (JoinHandler handler : this.joinHandlers.values())
      handler.onJoin(player);
  }

  public void onQuit(Player player) {
    for (JoinHandler handler : this.joinHandlers.values())
      handler.onQuit(player);

    // TODO: Unload player from all entites (scoreboards, permissions)
    // https://gitlab.com/BerryGames/BerryGamesCore/-/blob/master/src/main/java/net/berrygames/core/listeners/general/GeneralConnectionListener.java?ref_type=heads#L103
    this.unloadPlayer(player.getUniqueId());
  }

  public void loadPlayer(UUID uniqueId) {
    PlayerImpl player = new PlayerImpl(uniqueId);
    this.playersCache.put(uniqueId, player);
  }

  public void unloadPlayer(UUID uniqueId) {
    if (this.playersCache.containsKey(uniqueId))
      this.playersCache.get(uniqueId).save();

    this.playersCache.remove(uniqueId);
  }
}
