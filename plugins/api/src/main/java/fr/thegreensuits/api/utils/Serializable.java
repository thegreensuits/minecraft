package fr.thegreensuits.api.utils;

import com.google.gson.Gson;

public abstract class Serializable {
    protected final Gson gson = new Gson();

    public String serialize() {
        return gson.toJson(this);
    }

    public static <T> T deserialize(String json, Class<T> clazz) {
        return new Gson().fromJson(json, clazz);
    }

    public static <T> T deserialize(String json, T object) {
        return new Gson().fromJson(json, (Class<T>) object.getClass());
    }

    public static <T> T deserialize(String json, T object, Class<T> clazz) {
        return new Gson().fromJson(json, clazz);
    }
}
