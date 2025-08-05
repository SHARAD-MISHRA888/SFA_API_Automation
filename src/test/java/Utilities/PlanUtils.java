package Utilities;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

public class PlanUtils extends RestUtils {
    public static Map<String, List<Integer>> getAllPlanIds( String token,int memberId, LocalDate startDate, LocalDate endDate) {
        String baseUri = "https://staging.prism-sfa-dev.net";

        String fullUrl = "/combine-tour-plan/findByStartAndEndDateByMemberIdForBjpAndDjpAndCjp/" +
                memberId + "?startDate=" + startDate + "&endDate=" + endDate;

        System.out.println("Calling URL: " + baseUri + fullUrl);

        Response response = given()
                .baseUri(baseUri)
                .header("Authorization", "Bearer " +token)
                .header("accept", "application/hal+json")
                .get(fullUrl);

        response.then().statusCode(200);

        System.out.println("Full API Response:");
        System.out.println(response.asPrettyString());

        Map<String, List<Integer>> result = new HashMap<>();
        result.put("BJP", extractIdsFrom(response, "bjpReportResponseList", "planId"));
        result.put("DJP", extractIdsFrom(response, "doctorReportResponseList", "planId"));
        result.put("CJP", extractIdsFrom(response, "cjpReportResponseList", "id"));


        return result;
    }


    // Helper method to extract plan IDs from each list
    private static List<Integer> extractIdsFrom(Response response, String key, String idFieldName) {
        List<Map<String, Object>> plans = response.jsonPath().getList(key);

        if (plans == null) {
            System.out.println("No plans found for key: " + key);
            return Collections.emptyList();
        }

        return new ArrayList<>(
                plans.stream()
                        .map(plan -> (Integer) plan.get(idFieldName))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()) // or use TreeSet for sorted order
        );


    }


    public static void approvePlans(Map<String, List<Integer>> planIdsMap, String token) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("bjpPlanIds", planIdsMap.getOrDefault("BJP", Collections.emptyList()));
        payload.put("djpPlanIds", planIdsMap.getOrDefault("DJP", Collections.emptyList()));
        payload.put("cjpPlanId", planIdsMap.getOrDefault("CJP", Collections.emptyList())); // Note: key is singular in API: cjpPlanId

        payload.put("bjpStatusRemark", "Approved");
        payload.put("djpStatusRemark", "Approved");
        payload.put("cjpStatusRemark", "Approved");

        payload.put("bjpApprovalStatus", "Accepted");
        payload.put("djpApprovalStatus", "Accepted");
        payload.put("cjpApprovalStatus", "Accepted");

        Response response = given()
                .baseUri("https://staging.prism-sfa-dev.net")
                .basePath("/combine-tour-plan/updateApprovalStatus")
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(payload)
                .log().all()
                .when()
                .put()
                .then()
                .log().all()
                .extract().response();

        System.out.println("Approval Response Status Code: " + response.statusCode());
    }

}



