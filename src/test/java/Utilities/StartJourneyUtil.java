package Utilities;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

public class StartJourneyUtil {

    public static String uploadAttendanceImage(String base64Image, String token) {
        Map<String, String> payload = new HashMap<>();
        payload.put("imageUrl", base64Image);

        Response response = RestAssured.given()
                .baseUri("https://staging.prism-sfa-dev.net")
                .basePath("/uploadBase64")
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(payload)
                .log().all()
                .when()
                .post()
                .then()
                .log().all()
                .extract().response();

        String contentType = response.getContentType();
        if (contentType.contains("application/json")) {
            String result = response.jsonPath().getString("someKey");
        } else {
            String result = response.asString();  // fallback for plain text
        }

        String imageName = response.getBody().asString();
        System.out.println("Uploaded image name: " + imageName);
        return imageName;
    }

    public static void markAttendance(Map<String, Object> attendancePayload, String token) {
        Response response = RestAssured.given()
                .baseUri("https://staging.prism-sfa-dev.net")
                .basePath("/attendance")
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(attendancePayload)
                .log().all()
                .when()
                .post()
                .then()
                .log().all()
                .extract().response();

        System.out.println("Attendance Response Status Code: " + response.statusCode());
    }
}
