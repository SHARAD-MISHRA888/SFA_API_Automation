package Utilities;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class RestUtils {
    static {
        RestAssured.baseURI = "https://staging.prism-sfa-dev.net";
    }

    public static final String BASE64_IMAGE = "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"; // Keep this as-is
    public static String SALESPERSON_TOKEN;

    public static Response post(String endpoint,String payload){
        return RestAssured.given()
                .header("Authorization", "Bearer " +login("9918401438","Test@123"))
                .header("Accept", "application/hal+json")
                .header("Content-Type", "application/json")
                .body(payload)
                .post(endpoint);
    }

    public static Response put(String endpoint, String body) {
        return RestAssured.given()
                .header("Content-Type", "application/json")
                .body(body)
                .put(endpoint);
    }

    public static Response postWithAuth(String endpoint, String body, String token) {
        return RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body(body)
                .post(endpoint);
    }

    public static Response getWithAuth(String endpoint, String token) {
        return RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .get(endpoint);
    }

    public static Response deleteWithAuth(String endpoint, String token) {
        return RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .delete(endpoint);
    }

    public static String login(String mobile, String password) {
        String payload = "{ \"mobile\": \"" + mobile + "\", \"password\": \"" + password + "\"}";
         SALESPERSON_TOKEN = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(payload)
                .post("/authenticate")
                .then()
                .statusCode(200)
                .extract()
                .asString();
        System.out.println("Token received: " + SALESPERSON_TOKEN);
        return SALESPERSON_TOKEN;
    }
    public static void resetToken() {
        SALESPERSON_TOKEN = null;
    }

}
