package data.Payload.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStore {
    private static final Map<String, Object> data = new HashMap<>();

    public static void put(String key, Object value) {
        data.put(key, value);
    }

    public static boolean containsKey(String key) {
        return data.containsKey(key);
    }

    public static <T> T get(String key) {
        if (!data.containsKey(key)) {
            throw new IllegalStateException("Key not found in DataStore: " + key);
        }
        return (T) data.get(key);
    }

    public static <T> List<T> getList(String key) {
        if (!data.containsKey(key)) {
            throw new IllegalStateException("List key not found in DataStore: " + key);
        }
        Object value = data.get(key);
        if (value instanceof List<?>) {
            return (List<T>) value;
        }
        throw new IllegalStateException("Value for key '" + key + "' is not a List");
    }

    public static int getInt(String key) {
        if (!data.containsKey(key)) {
            throw new IllegalStateException("Int key not found in DataStore: " + key);
        }
        Object val = data.get(key);
        if (val instanceof Integer) {
            return (int) val;
        } else {
            throw new IllegalStateException("Value for key '" + key + "' is not an Integer");
        }
    }

    public static void clear() {
        data.clear();
    }
}

