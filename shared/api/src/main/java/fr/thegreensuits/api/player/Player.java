package fr.thegreensuits.api.player;

import java.util.UUID;

import lombok.Getter;

public abstract class Player {
  @Getter()
  private UUID id;

  @Getter()
  private String effectiveName, displayName;
}