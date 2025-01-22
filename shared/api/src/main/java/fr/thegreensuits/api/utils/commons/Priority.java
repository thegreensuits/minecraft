package fr.thegreensuits.api.utils.commons;

public enum Priority {
  LOWEST(0),
  LOW(1),
  NORMAL(2),
  HIGH(3),
  HIGHEST(4);

  private final int value;

  private Priority(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
