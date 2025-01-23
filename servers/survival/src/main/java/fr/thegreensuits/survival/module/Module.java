package fr.thegreensuits.survival.module;

public interface Module {
  default void onLoad() {
  }

  void onEnable();

  default void onDisable() {
  }
}