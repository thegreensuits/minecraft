package fr.thegreensuits.api.player;

import java.util.UUID;

public abstract class Player {
  public abstract UUID getUniqueId();

  public abstract String getEffectiveName();

  public abstract String getDisplayName();
}