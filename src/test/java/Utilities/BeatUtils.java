package Utilities;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class BeatUtils {
    public static Response getAllBeetsByMemberId(String token, int memberId, int page, int pageSize,
                                                 String sortBy, String sortDirection) {
        return RestAssured.given()
                .baseUri("https://staging.prism-sfa-dev.net")
                .header("Authorization", "Bearer " + token)
                .header("accept", "application/hal+json")
                .queryParam("page", page)
                .queryParam("pageSize", pageSize)
                .queryParam("sortBy", sortBy)
                .queryParam("sortDirection", sortDirection)
                .get("/getAllBeetsByMemberId/" + memberId);

    }
}
