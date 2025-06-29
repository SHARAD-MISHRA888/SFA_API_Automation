package Utilities;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class RestUtils {
    static {
        RestAssured.baseURI = "https://your-api-domain.com/api";
    }

    public static Response post(String endpoint, String body) {
        return RestAssured.given()
                .header("Content-Type", "application/json")
                .body(body)
                .post(endpoint);
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
}
