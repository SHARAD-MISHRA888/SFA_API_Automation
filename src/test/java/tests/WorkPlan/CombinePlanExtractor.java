package tests.WorkPlan;

import data.Payload.Response.DataStore;
import endpoints.Endpoints;
import io.restassured.RestAssured;
import Utilities.DBUtility;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

public class CombinePlanExtractor {


    private static final Map<Integer, double[]> outletLatLongMap = new HashMap<>();
    private static final Map<Integer, double[]> doctorLatLongMap = new HashMap<>();
    private static final Map<Integer, double[]> clientLatLongMap = new HashMap<>();

    public static void extractAllLogsAndPlanIds(String token, int memberId, String visitDate) {

        Response response = given().
                baseUri(Endpoints.BASE_URL)
                .header("Authorization", "Bearer " + token)
                .header("accept", "application/hal+json")
                .pathParam("memberId", memberId)
                .queryParam("visitDate", getTodayDate())
                .get(Endpoints.Get_Today_Plan);


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
            Integer logId = (log.get("id") instanceof Integer) ? (Integer) log.get("id") : null;

            Map<String, Object> outletDto = log.containsKey("outletGetDto")
                    ? (Map<String, Object>) log.get("outletGetDto") : null;
            Map<String, Object> doctorDto = log.containsKey("doctorRes")
                    ? (Map<String, Object>) log.get("doctorRes") : null;
            Map<String, Object> clientDto = log.containsKey("clientFMCGResponse")
                    ? (Map<String, Object>) log.get("clientFMCGResponse") : null;

            Integer outletId = (outletDto != null && outletDto.get("id") instanceof Integer)
                    ? (Integer) outletDto.get("id") : null;

            Integer clientId = (clientDto != null && clientDto.get("id") instanceof Integer)
                    ? (Integer) clientDto.get("id")
                    : (outletDto != null && outletDto.containsKey("clientId"))
                    ? (Integer) outletDto.get("clientId") : null;

            Integer doctorId = (doctorDto != null && doctorDto.get("id") instanceof Integer)
                    ? (Integer) doctorDto.get("id") : null;

            // ✅ Helper to fetch lat/long safely
            BiConsumer<String, Integer> fetchLatLong = (type, id) -> {
                if (id != null && logId != null) {
                    try {
                        String query;
                        switch (type.toLowerCase()) {
                            case "doctor":
                                query = "SELECT latitude, longitude FROM sfa_db.doctor WHERE id = ?;";
                                break;
                            case "client":
                                query = "SELECT latitude, longitude FROM sfa_db.clientfmcg WHERE id = ?;";
                                break;
                            default:
                                query = "SELECT latitude, longitude FROM sfa_db.outlet WHERE id = ?;";
                        }

                        double[] latLong = DBUtility.getLatLong(query, id);
                        if (latLong == null || latLong.length < 2) return;

                        switch (type.toLowerCase()) {
                            case "doctor":
                                doctorLatLongMap.put(logId, latLong);
                                break;
                            case "client":
                                clientLatLongMap.put(logId, latLong);
                                break;
                            default:
                                outletLatLongMap.put(logId, latLong);
                        }

                    } catch (Exception e) {
                        System.err.println("Failed to fetch lat/long for " + type + " with ID=" + id);
                        e.printStackTrace();
                    }
                }
            };

            fetchLatLong.accept("outlet", outletId);
            fetchLatLong.accept("client", clientId);
            fetchLatLong.accept("doctor", doctorId);
        }




        String workingType = jsonPath.getString("bjpReportResponseList[0].workingWith");

        DataStore.put("WorkingWith", workingType);


        DataStore.put("clientFmcgJourneyPlanStatus", clientFmcgJourneyPlanStatus);
        DataStore.put("doctorJourneyPlanStatus", doctorJourneyPlanStatus);
        DataStore.put("beetJourneyPlanStatus", beetJourneyPlanStatus);

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
    private static String getTodayDate () {
        return java.time.LocalDate.now().toString();
    }

    public static double[] getOutletLatLong(int logId) {
        return outletLatLongMap.get(logId);
    }

    public static double[] getDoctorLatLong(int logId) {
        return doctorLatLongMap.get(logId);
    }

    public static double[] getClientLatLong(int logId) {
        return clientLatLongMap.get(logId);
    }
}

