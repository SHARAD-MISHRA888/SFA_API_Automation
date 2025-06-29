package Utilities;

import java.util.*;

public class DataStore {
    private static final Map<String, Object> data = new HashMap<>();

    public static void put(String key, Object value) {
        data.put(key, value);
    }

    public static <T> T get(String key) {
        return (T) data.get(key);
    }

    public static <T> List<T> getList(String key) {
        return (List<T>) data.getOrDefault(key, new ArrayList<>());
    }

    public static int getInt(String key) {
        Object val = data.get(key);
        return val instanceof Integer ? (int) val : 0;
    }
}