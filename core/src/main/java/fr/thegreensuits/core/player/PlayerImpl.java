package fr.thegreensuits.core.player;

import java.util.UUID;

import fr.thegreensuits.api.player.Player;

public class PlayerImpl extends Player {
  public final UUID uniqueId;
  public String effectiveName, displayName;

  public PlayerImpl(UUID uniqueId) {
    this.uniqueId = uniqueId;

    this.load();
  }

  public void load() {
    // TODO: Load player data from database
    this.refresh();
  }

  public void refresh() {
    // TODO: Refresh player data from database
  }

  public void save() {
    // TODO: Save player data to database
  }

  @Override
  public UUID getUniqueId() {
    return this.uniqueId;
  }

  @Override
  public String getEffectiveName() {
    return this.effectiveName;
  }

  @Override
  public String getDisplayName() {
    return this.displayName;
  }
}
