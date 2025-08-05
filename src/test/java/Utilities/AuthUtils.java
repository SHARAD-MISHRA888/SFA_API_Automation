package Utilities;

import io.restassured.RestAssured;

public class AuthUtils {
    public static String login(String mobile, String password) {
        String payload = "{ \"mobile\": \"" + mobile + "\", \"password\": \"" + password + "\"}";
        String token = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(payload)
                .post("/authenticate")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        System.out.println("Token received: " + token);
        return token;
    }
}
