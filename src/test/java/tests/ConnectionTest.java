package tests;
import java.net.HttpURLConnection;
import java.net.URL;
public class ConnectionTest {

    public static void main(String[] args) {
        try {
            URL url = new URL("https://staging.prism-sfa-dev.net/attendance");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            System.out.println("Response code: " + responseCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
