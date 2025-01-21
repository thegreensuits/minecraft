package fr.thegreensuits.api.config;

import lombok.Getter;

public class RedisConfig {
  @Getter()
  private String host, password;
  @Getter()
  private int port, database;
  @Getter()
  private boolean enabled;

  public RedisConfig(boolean enabled, String host, int port, int database, String password) {
    this.enabled = enabled;
    this.host = host;
    this.port = port;
    this.database = database;
    this.password = password;
  }
}
