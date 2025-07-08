package Utilities;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

public class CombinePlanExtractor {

    public static void extractAllLogsAndPlanIds(String token, int memberId, String visitDate) {
        RestAssured.baseURI = "https://staging.prism-sfa-dev.net";

        Response response = given()
                .header("Authorization", "Bearer " + token)
                .header("accept", "application/hal+json")
                .when()
                .get("/combine-tour-plan/getTodayCombinePlanByMemberId/" + memberId + "?visitDate=" + visitDate);

        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Failed to fetch combine plan: " + response.statusLine());
        }

        JsonPath jsonPath = response.jsonPath();

        // Extract beet logs (BJP)
        List<Integer> beetLogIds = jsonPath.getList("bjpReportResponseList.id");
        List<Integer> beetPlanIds = jsonPath.getList("bjpReportResponseList.planId");
        if (beetLogIds != null) {
            DataStore.put("beetLogIds", beetLogIds.stream().map(String::valueOf).collect(Collectors.toList()));
        }
        if (beetPlanIds != null && !beetPlanIds.isEmpty()) {
            DataStore.put("beetLogPlanId", beetPlanIds.get(0)); // or handle multiple if needed
        }


        List<Map<String, Object>> bjpList = jsonPath.getList("bjpReportResponseList");
        if (bjpList != null && !bjpList.isEmpty()) {
            Map<String, Object> firstItem = bjpList.get(0);
            Map<String, Object> beet = (Map<String, Object>) firstItem.get("beet");
            if (beet != null && beet.get("id") != null) {
                int beetId = (Integer) beet.get("id");
                DataStore.put("bjp_beet_id", beetId); // use a dedicated key
                System.out.println("Extracted beetId from combine plan: " + beetId);
            }
        }

        // Extract doctor logs (DJP)
        List<Integer> doctorLogIds = jsonPath.getList("doctorReportResponseList.id");
        List<Integer> doctorPlanIds = jsonPath.getList("doctorReportResponseList.planId");
        if (doctorLogIds != null) {
            DataStore.put("doctorLogIds", doctorLogIds.stream().map(String::valueOf).collect(Collectors.toList()));
        }
        if (doctorPlanIds != null && !doctorPlanIds.isEmpty()) {
            DataStore.put("doctorPlanId", doctorPlanIds.get(0)); // or handle multiple
        }

        // Extract client FMCG logs (CJP)
        List<Integer> clientLogIds = jsonPath.getList("cjpReportResponseList.id");
        if (clientLogIds != null) {
            DataStore.put("clientLogIds", clientLogIds.stream().map(String::valueOf).collect(Collectors.toList()));
        }

        // Put memberId as well
        DataStore.put("memberId", memberId);

        System.out.println(response.asPrettyString());

        System.out.println("Extracted all logs and plan IDs successfully.");

    }
}

