package fr.thegreensuits.api.utils;

import com.google.gson.Gson;

public abstract class Serializable {
    protected static final Gson gson = new Gson();

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
