package tests.Sync;

import Utilities.RestUtils;
import endpoints.Endpoints;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.Payload.Request.AutoDataGenerator;
import data.Payload.Response.DataStore;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import tests.WorkPlan.CombinePlanExtractor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static io.restassured.RestAssured.given;

public class SyncMemberLogUtil extends RestUtils {

    private static Random random = new Random();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private static LocalDateTime currentTime = LocalDateTime.of(2025, 7, 3, 10, 0);

    public static Response syncLogsAndEndDay(String token) {
        Map<String, Object> payload = buildSyncPayload();

        return given()
                .baseUri(Endpoints.BASE_URL)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body(payload)
                .when()
                .post(Endpoints.Sync_Member_Logs);
    }

    public static Map<String, Object> buildSyncPayload() {
        Map<String, Object> payload = new HashMap<>();
        String workType = DataStore.get("workWith");

        // Safe defaults if keys are missing
        int memberId = DataStore.containsKey("memberId") ? getIntOrThrow("memberId") : -1;
        int doctorPlanId = DataStore.containsKey("doctorPlanId") ? getIntOrThrow("doctorPlanId") : -1;
        int beetLogPlanId = DataStore.containsKey("beetLogPlanId") ? getIntOrThrow("beetLogPlanId") : -1;

        // Logs

            addLogListToPayload(payload, "beetLogIds", "beetLogReqList", "beet");
            addLogListToPayload(payload, "doctorLogIds", "doctorLogReqList", "doctor");
            addLogListToPayload(payload, "clientLogIds", "clientFmcgLogReqList", "client");


        // Optional order and sample lists
        List<Map<String, Object>> orders = new ArrayList<>();
        List<Map<String, Object>> samples = new ArrayList<>();
        try {
            orders = DataStore.getList("orders");
            samples = DataStore.getList("samples");
        } catch (IllegalStateException ignored) {}


        payload.put("orderSynReqList", orders);
        payload.put("sampleReqList", samples);

        payload.put("memberId", memberId);
        payload.put("doctorPlanId", doctorPlanId);
        payload.put("beetLogPlanId", beetLogPlanId);
//        payload.put("isStay", false);
        payload.put("endYourDay", true);
        payload.put("stay", false);

        // Expense Section
        Map<String, Object> expense = new HashMap<>();
        expense.put("memberId", memberId);
        expense.put("date", LocalDate.now().toString());
        expense.put("workingWith", "Member");
        expense.put("vehicleOwnerId", memberId);
        expense.put("otherMemberIds", new ArrayList<>());
        expense.put("modeOfTransport", "CAR");

        payload.put("expenseWithTaAndDaReq", expense);
        return payload;
    }

    private static void addLogListToPayload(Map<String, Object> payload, String dataStoreKey, String payloadKey, String type) {
        List<Map<String, Object>> logReqList = new ArrayList<>();

        try {
            List<String> logIds = DataStore.getList(dataStoreKey);
            for (String id : logIds) {
                logReqList.add(buildLog(id, type));
            }
        } catch (IllegalStateException ignored) {
            // If no logIds found, it stays empty
        }

        payload.put(payloadKey, logReqList); // always put the list, even if it's empty
    }



    private static Map<String, Object> buildLog(String logId, String type) {
        Map<String, Object> log = new HashMap<>();
        int intLogId = Integer.parseInt(logId);

        // Randomized times
        LocalDateTime checkIn = currentTime.plusMinutes(random.nextInt(15));
        LocalDateTime checkOut = checkIn.plusMinutes(30 + random.nextInt(30));
        currentTime = checkOut.plusMinutes(10);

        double[] latLong = null;
        switch (type.toLowerCase()) {
            case "doctor":
                latLong = CombinePlanExtractor.getDoctorLatLong(intLogId);
                log.put("doctorLogId", intLogId);
                break;
            case "client":
                latLong = CombinePlanExtractor.getClientLatLong(intLogId);
                log.put("clientFmcgLogId", intLogId);
                break;
            default: // outlet (BJP case)
                latLong = CombinePlanExtractor.getOutletLatLong(intLogId);
                log.put("beetLogId", intLogId);
        }

        if (latLong != null && latLong.length == 2) {
            log.put("latitude", latLong[0]);
            log.put("longitude", latLong[1]);
        } else {
            log.put("latitude", 0.0);   // fallback
            log.put("longitude", 0.0);  // fallback
        }
        log.put("checkIn", checkIn.format(FORMATTER));
        log.put("checkOut", checkOut.format(FORMATTER));
        log.put("remark", "");
        log.put("remainder", false);
        log.put("remainderDate", null);
        log.put("status", "Completed");

        return log;
    }

    public static void printPayloadForDebugging() {
        try {
            Map<String, Object> payload = buildSyncPayload();
            String pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(payload);
            System.out.println("SYNC PAYLOAD BEFORE POSTING:\n" + pretty);
        } catch (IllegalStateException e) {
            System.err.println("Missing required data: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Failed to print sync payload: " + e.getMessage());
        }
    }


    private static int getIntOrThrow(String key) {
        int val = DataStore.getInt(key);
        if (val == 0) {
            throw new IllegalStateException("Integer key not found or is 0 in DataStore: " + key);
        }
        return val;
    }


    public static Response updateWorkingWith(String token) {
        // Build the payload
        Map<String, Object> payload = updateWorkingStatus();


        // Perform the PUT request using RestAssured
        Response response = RestAssured.given()
                .baseUri(Endpoints.BASE_URL) // 🔁 Base URL
                .basePath(Endpoints.Update_Working_with)    // 🔁 Path if needed separately
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body(payload)
                .log().all()
                .when()
                .put() // PUT method
                .then()
                .log().all()
                .extract()
                .response();

        return response;
    }


    public static Map<String,Object> updateWorkingStatus(){

        Map<String,Object> payload = new HashMap<>();
        String workType = DataStore.get("WorkingWith");

        payload.put("workingWith",workType);
        payload.put("date",LocalDate.now().toString());
        payload.put("remark","All Completed");
        payload.put("otherMemberIds",new ArrayList<>());
        payload.put("memberId",DataStore.get("memberId"));
        payload.put("getVehicleOwnerId",DataStore.get("memberId"));
        payload.put("modeOfTransport","CAR");
        payload.put("removedMemberIds",new ArrayList<>());

        return payload;


    }

}
