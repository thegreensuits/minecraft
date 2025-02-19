package fr.thegreensuits.api.utils.serialization;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Custom deserializer for any enum type.
 * 
 * @param <T> The enum type to deserialize
 */
public class EnumDeserializer<T extends Enum<T>> implements JsonDeserializer<T> {
  private final Class<T> enumClass;

  public EnumDeserializer(Class<T> enumClass) {
    this.enumClass = enumClass;
  }

  @Override
  public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    if (json.isJsonNull()) {
      return null;
    }

    String value = json.getAsString();
    try {
      // Try standard enum parsing (case-sensitive)
      return Enum.valueOf(enumClass, value);
    } catch (IllegalArgumentException e) {
      // Try case-insensitive matching as a fallback
      for (T enumValue : enumClass.getEnumConstants()) {
        if (enumValue.name().equalsIgnoreCase(value)) {
          return enumValue;
        }
      }

      throw new JsonParseException("Unknown enum value: " + value + " for enum " + enumClass.getName());
    }
  }
}