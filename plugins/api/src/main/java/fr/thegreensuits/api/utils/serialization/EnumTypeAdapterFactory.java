package fr.thegreensuits.api.utils.serialization;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A TypeAdapterFactory for handling enum deserialization with
 * case-insensitivity
 */
public class EnumTypeAdapterFactory implements TypeAdapterFactory {
  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
    Class<? super T> rawType = typeToken.getRawType();
    if (!Enum.class.isAssignableFrom(rawType) || rawType == Enum.class) {
      return null;
    }

    @SuppressWarnings("unchecked")
    Class<T> enumClass = (Class<T>) rawType;

    return new TypeAdapter<T>() {
      @Override
      public void write(JsonWriter out, T value) throws IOException {
        if (value == null) {
          out.nullValue();
        } else {
          out.value(((Enum<?>) value).name());
        }
      }

      @Override
      public T read(JsonReader in) throws IOException {
        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
          in.nextNull();
          return null;
        }

        String value = in.nextString();

        try {
          @SuppressWarnings("unchecked")
          T result = (T) Enum.valueOf((Class<? extends Enum>) enumClass, value);
          return result;
        } catch (IllegalArgumentException e) {
          // Try case-insensitive as fallback
          for (Object enumConstant : enumClass.getEnumConstants()) {
            if (((Enum<?>) enumConstant).name().equalsIgnoreCase(value)) {
              @SuppressWarnings("unchecked")
              T result = (T) enumConstant;
              return result;
            }
          }

          throw new JsonSyntaxException("Unknown enum value: " + value + " for enum " + enumClass.getName());
        }
      }
    }.nullSafe();
  }
}
