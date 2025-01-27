package fr.thegreensuits.core;

import fr.thegreensuits.api.TheGreenSuits;
import fr.thegreensuits.api.config.RedisConfig;
import fr.thegreensuits.api.player.PlayerManager;
import fr.thegreensuits.core.player.event.PlayerManagerImpl;

public class TheGreenSuitsImpl extends TheGreenSuits {
  protected final PlayerManagerImpl playerManager;

  public TheGreenSuitsImpl(RedisConfig redisConfig) {
    super(redisConfig);

    this.playerManager = new PlayerManagerImpl(); // new PlayerManagerImpl(this); ????
  }

  @Override
  public PlayerManager getPlayerManager() {
    return this.playerManager;
  }
}
