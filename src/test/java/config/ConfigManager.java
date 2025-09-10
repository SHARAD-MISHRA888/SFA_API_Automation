package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {
    private static Properties props = new Properties();

    static {
        try {
            FileInputStream fis = new FileInputStream("src/test/resources/config.properties");
            props.load(fis);

        }catch (IOException e){
            throw new RuntimeException("Failed to load config,properties",e);
        }
    }

    public static String get(String key){
        return props.getProperty(key);
    }
}