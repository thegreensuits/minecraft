package fr.thegreensuits.api.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fr.thegreensuits.api.utils.serialization.EnumTypeAdapterFactory;

public abstract class Serializable {
    protected static final Gson gson = createGson();

    // Create a properly configured Gson instance
    private static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapterFactory(new EnumTypeAdapterFactory())
                .create();
    }

    public String serialize() {
        return Serializable.gson.toJson(this);
    }

    public static <T> T deserialize(String json, Class<T> clazz) {
        return Serializable.gson.fromJson(json, clazz);
    }

    public static <T> T deserialize(String json, T object) {
        return Serializable.gson.fromJson(json, (Class<T>) object.getClass());
    }

    public static <T> T deserialize(String json, T object, Class<T> clazz) {
        return Serializable.gson.fromJson(json, clazz);
    }

    public String toString() {
        return this.serialize();
    }
}
