package Utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.time.LocalDate;
import java.util.*;

import static io.restassured.RestAssured.given;

public class SyncMemberLogUtil {

    public static Response syncLogsAndEndDay(String token) {
        Map<String, Object> payload = buildSyncPayload();
        RestAssured.baseURI = "https://staging.prism-sfa-dev.net";

        return given()
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body(payload)
                .when()
                .post("/sync/syncMemberLogs");
    }

    public static Map<String, Object> buildSyncPayload() {
        Map<String, Object> payload = new HashMap<>();

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
        log.put("latitude", 26.4448);
        log.put("longitude", 80.3686);
        log.put("checkIn", "2025-07-03T10:00:00.000Z");
        log.put("checkOut", "2025-07-03T11:00:00.000Z");
        log.put("remark", "");
        log.put("remainder", false);
        log.put("remainderDate", null);
        log.put("status", "Completed");

        switch (type) {
            case "doctor": log.put("doctorLogId", intLogId); break;
            case "client": log.put("clientFmcgLogId", intLogId); break;
            default:        log.put("beetLogId", intLogId); break;
        }

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
}
