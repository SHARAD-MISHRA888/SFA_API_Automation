package tests.WorkPlan;

import data.Payload.Response.DataStore;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.ArrayList;
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

        String beetJourneyPlanStatus = jsonPath.getString("bjpReportResponseList[0].beetJourneyPlanStatus");
        String doctorJourneyPlanStatus = jsonPath.getString("doctorReportResponseList[0].doctorJourneyPlanStatus");
        String clientFmcgJourneyPlanStatus = jsonPath.getString("cjpReportResponseList[0].clientFmcgJourneyPlanStatus");


        List<Map<String, Object>> bjpList = response.jsonPath().getList("bjpReportResponseList");
        List<Map<String, Object>> djpList = response.jsonPath().getList("doctorReportResponseList");
        List<Map<String, Object>> cjpList = response.jsonPath().getList("cjpReportResponseList");

        List<Map<String, Object>> allLogs = new ArrayList<>();
        if (bjpList != null) allLogs.addAll(bjpList);
        if (djpList != null) allLogs.addAll(djpList);
        if (cjpList != null) allLogs.addAll(cjpList);

        for (Map<String, Object> log : allLogs) {
            Map<String, Object> outletDto = log.containsKey("outletGetDto") ? (Map<String, Object>) log.get("outletGetDto") : null;
            Map<String, Object> doctorDto = log.containsKey("doctorRes") ? (Map<String, Object>) log.get("doctorRes") : null;
            Map<String, Object> clientDto = log.containsKey("clientFMCGResponse") ? (Map<String, Object>) log.get("clientFMCGResponse") : null;
            Map<String, Object> beetDto = log.containsKey("beet") ? (Map<String, Object>) log.get("beet") : null;
            String workWithDto = log.containsKey("workingWith") ? (String) log.get("workingWith") : null;

            String outletLat = (outletDto != null && outletDto.containsKey("latitude"))
                    ? outletDto.get("latitude").toString()
                    : null;
            String outletLong = (outletDto != null && outletDto.containsKey("longitude"))
                    ? outletDto.get("longitude").toString()
                    : null;
            String docLat = (doctorDto != null && doctorDto.containsKey("latitude"))
                    ? doctorDto.get("latitude").toString() :
                    null;
            String docLong = (doctorDto != null && doctorDto.containsKey("longitude"))
                    ? doctorDto.get("longitude").toString() :
                    null;
            String clientLat = (clientDto != null && clientDto.containsKey("latitude"))
                    ? clientDto.get("latitude").toString() :
                    null;
            String clientLong = (clientLat != null && clientDto.containsKey("longitude"))
                    ? clientDto.get("longitude").toString() :
                    null;

            DataStore.put("outletLat", outletLat);
            DataStore.put("OutletLong", outletLong);
            DataStore.put("docLat", docLat);
            DataStore.put("docLong", docLong);
            DataStore.put("clientLat", clientLat);
            DataStore.put("clientLong", clientLong);
        }




        String workingType = jsonPath.getString("bjpReportResponseList[0].workingWith");

        DataStore.put("WorkingWith",workingType);


        DataStore.put("clientFmcgJourneyPlanStatus",clientFmcgJourneyPlanStatus);
        DataStore.put("doctorJourneyPlanStatus",doctorJourneyPlanStatus);
        DataStore.put("beetJourneyPlanStatus",beetJourneyPlanStatus);

        // Extract beet logs (BJP)
        List<Integer> beetLogIds = jsonPath.getList("bjpReportResponseList.id");
        List<Integer> beetPlanIds = jsonPath.getList("bjpReportResponseList.planId");
        if (beetLogIds != null) {
            DataStore.put("beetLogIds", beetLogIds.stream().map(String::valueOf).collect(Collectors.toList()));
        }
        if (beetPlanIds != null && !beetPlanIds.isEmpty()) {
            DataStore.put("beetLogPlanId", beetPlanIds.get(0)); // or handle multiple if needed
        }


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

