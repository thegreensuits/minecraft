package fr.thegreensuits.proxy.config;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;

public class ConfigManager {
  private final Path configPath;
  private ConfigurationNode root;
  private YamlConfigurationLoader loader;

  public ConfigManager(Path dataFolder) {
    this.configPath = dataFolder.resolve("config.yml");

    this.loader = YamlConfigurationLoader.builder()
        .path(configPath) // set the config file path
        .build();
  }

  public void loadConfig() {
    try {
      root = loader.load();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getServerId() {
    return root.node("server", "id").getString("-1");
  }
}
