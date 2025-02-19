package fr.thegreensuits.api.utils;

/**
 * Abstract class that implements the singleton pattern with a static instance.
 * Classes extending this will automatically have a static instance and getter.
 * 
 * @param <T> The type of the class extending StaticInstance
 */
public abstract class StaticInstance<T extends StaticInstance<T>> {

  // The static instance that will hold the singleton
  private static final StaticInstance<?>[] _INSTANCES = new StaticInstance<?>[100]; // Array to hold instances of
                                                                                    // different types

  /**
   * Constructor that registers this instance in the static registry.
   * Each subclass will have its own unique class ID for indexing.
   */
  protected StaticInstance() {
    int classId = getClassId();
    if (classId >= _INSTANCES.length || classId < 0) {
      throw new IllegalArgumentException("Class ID out of bounds: " + classId);
    }

    if (_INSTANCES[classId] != null) {
      throw new IllegalStateException("Instance for class ID " + classId + " already exists");
    }

    _INSTANCES[classId] = this;
  }

  /**
   * Each subclass must provide a unique class ID.
   * This ID is used to store and retrieve the instance from the static array.
   * 
   * @return A unique integer ID for the implementing class
   */
  protected abstract int getClassId();

  /**
   * Gets the static instance of the specified class.
   * 
   * @param <E>   The type of class extending StaticInstance
   * @param clazz The class to get the instance of
   * @return The singleton instance of the specified class
   * @throws IllegalStateException if the instance hasn't been initialized
   */
  @SuppressWarnings("unchecked")
  public static <E extends StaticInstance<E>> E get(Class<E> clazz) {
    try {
      // Create a temporary instance just to get the class ID
      E temp = clazz.getDeclaredConstructor().newInstance();
      int classId = temp.getClassId();

      // Check if we already have an instance
      if (_INSTANCES[classId] != null) {
        return (E) _INSTANCES[classId];
      }

      // Return the newly created instance
      return temp;
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to get instance of " + clazz.getName(), e);
    }
  }
}
