package Utilities;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

public class PlanUtils {

    public static Response createPlanInBulk(String token, Map<String, Object> payload) {
        return given()
                .baseUri("https://staging.prism-sfa-dev.net")
                .header("Authorization", "Bearer " + token)
                .header("accept", "application/hal+json")
                .header("Content-Type", "application/json")
                .body(payload)
                .post("/combine-tour-plan/createCombineTourPlanInBulk");
    }

    public static Map<String, List<Integer>> getAllPlanIds(String token, int memberId, LocalDate startDate, LocalDate endDate) {
        String baseUri = "https://staging.prism-sfa-dev.net";

        String fullUrl = "/combine-tour-plan/findByStartAndEndDateByMemberIdForBjpAndDjpAndCjp/" +
                memberId + "?startDate=" + startDate + "&endDate=" + endDate;

        System.out.println("Calling URL: " + baseUri + fullUrl);

        Response response = given()
                .baseUri(baseUri)
                .header("Authorization", "Bearer " + token)
                .header("accept", "application/hal+json")
                .get(fullUrl);

        response.then().statusCode(200);

        System.out.println("Full API Response:");
        System.out.println(response.asPrettyString());

        Map<String, List<Integer>> result = new HashMap<>();
        result.put("BJP", extractIdsFrom(response, "bjpReportResponseList", "planId"));
        result.put("DJP", extractIdsFrom(response, "doctorReportResponseList", "planId"));
        result.put("CJP", extractIdsFrom(response, "cjpReportResponseList", "id"));

//        System.out.println("BJP Plan IDs: " + result.get("BJP"));
//        System.out.println("DJP Plan IDs: " + result.get("DJP"));
//        System.out.println("CJP IDs: " + result.get("CJP"));

        return result;
    }


    // 🔁 Helper method to extract plan IDs from each list
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



    public static Map<String, Object> getTodayTourPlan(int memberId, String visitDate, String token) {
        Response response = given()
                .header("Authorization", "Bearer " + token)
                .accept("application/hal+json")
                .get("https://staging.prism-sfa-dev.net/combine-tour-plan/getTodayCombinePlanByMemberId/" + memberId + "?visitDate=" + visitDate);

       response.prettyPrint();

//
//        System.out.println(response);
//
//        Map<String, Object> result = new HashMap<>();
//        String workingWith = response.jsonPath().getString("workingWith.name");
//        result.put("workingWith", workingWith != null ? workingWith : "N/A");
//
//        // Safely extract beatId
//        List<Map<String, Object>> beats = response.jsonPath().getList("beats");
//        if (beats != null && !beats.isEmpty() && beats.get(0).get("id") != null) {
//            result.put("beatId", beats.get(0).get("id"));
//        } else {
//            result.put("beatId", null);  // No beat found
//        }
//
//        return result;

//   BJP Response List Only
        Map<String, Object> resultBjp = new HashMap<>();

        List<Map<String, Object>> bjpList = response.jsonPath().getList("bjpReportResponseList");

        if (bjpList != null && !bjpList.isEmpty()) {
            Map<String, Object> firstItem = bjpList.get(0);

            // Extract workingWith
            String workingWith = (String) firstItem.get("workingWith");
            resultBjp.put("workingWith", workingWith != null ? workingWith : "N/A");

            // Extract beetLogId directly from firstItem (top-level)
            Object beetLogId = firstItem.get("id"); // usually this is beetLogId
            resultBjp.put("beetLogId", beetLogId != null ? beetLogId : null);

            // Extract beetId from nested object
            Map<String, Object> beet = (Map<String, Object>) firstItem.get("beet");
            if (beet != null && beet.get("id") != null) {
                resultBjp.put("beetId", beet.get("id"));
            } else {
                resultBjp.put("beetId", null);
            }

        } else {
            System.out.println("❌ No BJP report data found.");
        }
// CJP Respnse List
        Map<String, Object> resultCjp = new HashMap<>();


        List<Map<String, Object>> cjpList = response.jsonPath().getList("bjpReportResponseList");

        if (bjpList != null && !bjpList.isEmpty()) {
            Map<String, Object> firstItem = bjpList.get(0);

            // Extract workingWith
            String workingWith = (String) firstItem.get("workingWith");
            resultCjp.put("workingWith", workingWith != null ? workingWith : "N/A");

            // Extract beetLogId directly from firstItem (top-level)
            Object beetLogId = firstItem.get("id"); // usually this is beetLogId
            resultCjp.put("beetLogId", beetLogId != null ? beetLogId : null);

            // Extract beetId from nested object
            Map<String, Object> beet = (Map<String, Object>) firstItem.get("beet");
            if (beet != null && beet.get("id") != null) {
                resultCjp.put("beetId", beet.get("id"));
            } else {
                resultCjp.put("beetId", null);
            }

        } else {
            System.out.println("❌ No BJP report data found.");
            resultCjp.put("workingWith", "N/A");
            resultCjp.put("beetId", null);
            resultCjp.put("beetLogId", null);
        }
// DJP Response List
        Map<String, Object> resultDjp = new HashMap<>();

        List<Map<String, Object>> djpList = response.jsonPath().getList("bjpReportResponseList");

        if (bjpList != null && !bjpList.isEmpty()) {
            Map<String, Object> firstItem = bjpList.get(0);

            // Extract workingWith
            String workingWith = (String) firstItem.get("workingWith");
            resultDjp.put("workingWith", workingWith != null ? workingWith : "N/A");

            // Extract beetLogId directly from firstItem (top-level)
            Object beetLogId = firstItem.get("id"); // usually this is beetLogId
            resultDjp.put("beetLogId", beetLogId != null ? beetLogId : null);

            // Extract beetId from nested object
            Map<String, Object> beet = (Map<String, Object>) firstItem.get("beet");
            if (beet != null && beet.get("id") != null) {
                resultDjp.put("beetId", beet.get("id"));
            } else {
                resultDjp.put("beetId", null);
            }

        } else {
            System.out.println("❌ No BJP report data found.");
            resultDjp.put("workingWith", "N/A");
            resultDjp.put("beetId", null);
            resultDjp.put("beetLogId", null);
        }

        return resultDjp;
    }



//    public static Map<String, Object> getTodayTourPlan(int memberId, String visitDate, String token) {
//        RestAssured.baseURI = "https://staging.prism-sfa-dev.net";
//        Response response = given()
//                .header("Authorization", "Bearer " + token)
//                .get("/combine-tour-plan/getTodayCombinePlanByMemberId/" + memberId + "?visitDate=" + visitDate);
//
//        if (response.statusCode() != 200) {
//            System.out.println("❌ Failed to fetch tour plan. Status: " + response.statusCode());
//            return Collections.emptyMap();
//        }
//
//        Map<String, Object> fullResponse = response.jsonPath().getMap("");
//
//        // You can pass this entire response to AutoDataGenerator for further parsing
//        return fullResponse;
//    }



    public static Response getSampleInventoryByMemberId(int memberId, int page, int pageSize, String sortBy, String sortDirection, String token) {
        String endpoint = String.format("https://staging.prism-sfa-dev.net/inventory-service/sample-inventory/getAllSampleMemberById/9?page=0&pageSize=500&sortBy=createdDate&sortDirection=dsc",
                memberId, page, pageSize, sortBy, sortDirection);

        return RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .header("accept", "application/hal+json")
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();
    }

    public static Response getProductInventory(int clientId, int page, int pageSize, String sortBy, String sortDirection, String token) {
        String endpoint = String.format("https://staging.prism-sfa-dev.net/inventory-service/inventoryWithProductNameWithClientFmcgResponse/all/%d?page=%d&pageSize=%d&sortBy=%s&sortDirection=%s",
                clientId, page, pageSize, sortBy, sortDirection);

        return RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .header("accept", "application/hal+json")
                .get(endpoint);
    }

}



