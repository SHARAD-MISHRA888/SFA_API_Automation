package config;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.List;

public class MemberConfigLoader {
    public static List<String> loadMemberMobiles(String filePath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(new File(filePath), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to load member config: " + filePath, e);
        }
    }

}
