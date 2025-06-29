package Utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
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

    private static Map<String, Object> buildSyncPayload() {
        Map<String, Object> payload = new HashMap<>();

        // Fetch from DataStore
        List<String> beetLogIds = DataStore.getList("beetLogIds");
        List<String> doctorLogIds = DataStore.getList("doctorLogIds");
        List<String> clientLogIds = DataStore.getList("clientLogIds");
        List<Map<String, Object>> orders = DataStore.getList("orders");
        List<Map<String, Object>> samples = DataStore.getList("samples");
        int memberId = DataStore.getInt("memberId");
        int doctorPlanId = DataStore.getInt("doctorPlanId");
        int beetLogPlanId = DataStore.getInt("beetLogPlanId");

        // Build logs
        List<Map<String, Object>> beetLogReqList = new ArrayList<>();
        for (String id : beetLogIds) {
            beetLogReqList.add(buildLog(id, "beet"));
        }

        List<Map<String, Object>> doctorLogReqList = new ArrayList<>();
        for (String id : doctorLogIds) {
            doctorLogReqList.add(buildLog(id, "doctor"));
        }

        List<Map<String, Object>> clientFmcgLogReqList = new ArrayList<>();
        for (String id : clientLogIds) {
            clientFmcgLogReqList.add(buildLog(id, "client"));
        }

        payload.put("beetLogReqList", beetLogReqList);
        payload.put("doctorLogReqList", doctorLogReqList);
        payload.put("clientFmcgLogReqList", clientFmcgLogReqList);
        payload.put("orderSynReqList", orders);
        payload.put("sampleReqList", samples);
        payload.put("memberId", memberId);
        payload.put("doctorPlanId", doctorPlanId);
        payload.put("beetLogPlanId", beetLogPlanId);
        payload.put("isStay", false);
        payload.put("endYourDay", true);
        payload.put("stay", false);

        Map<String, Object> expense = new HashMap<>();
        expense.put("memberId", memberId);
        expense.put("date", "2025-06-24");
        expense.put("workingWith", "Self");
        expense.put("vehicleOwnerId", memberId);
        expense.put("otherMemberIds", new ArrayList<>());
        expense.put("modeOfTransport", "CAR");

        payload.put("expenseWithTaAndDaReq", expense);
        return payload;
    }

    private static Map<String, Object> buildLog(String logId, String type) {
        Map<String, Object> log = new HashMap<>();
        log.put("latitude", 26.4448);
        log.put("longitude", 80.3686);
        log.put("checkIn", "2025-06-24T11:28:59.623Z");
        log.put("checkOut", "2025-06-24T11:29:18.987Z");
        log.put("remark", "");
        log.put("remainder", false);
        log.put("remainderDate", null);
        log.put("status", "Completed");

        if (type.equals("doctor")) {
            log.put("doctorLogId", logId);
        } else if (type.equals("client")) {
            log.put("clientFmcgLogId", logId);
        } else {
            log.put("beetLogId", logId);
        }
        return log;
    }

    public static void printPayloadForDebugging() {
        try {
            Map<String, Object> payload = buildSyncPayload();
            String pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(payload);
            System.out.println("SYNC PAYLOAD BEFORE POSTING:\n" + pretty);
        } catch (Exception e) {
            System.err.println("Failed to print sync payload: " + e.getMessage());
        }
    }



//    public static Response syncLogsAndEndDay(String token) {
//        Map<String, Object> payload = buildStaticSyncPayload();
//
//        RestAssured.baseURI = "https://staging.prism-sfa-dev.net";
//
//        return given()
//                .header("Authorization", "Bearer " + token)
//                .header("Content-Type", "application/json")
//                .body(payload)
//                .when()
//                .post("/sync/syncMemberLogs");
//    }
//
//    private static Map<String, Object> buildStaticSyncPayload() {
//        Map<String, Object> payload = new HashMap<>();
//
//        // --- Static Log Data
//        List<Map<String, Object>> beetLogReqList = Arrays.asList(
//                buildLog("1085", "beet", "2025-06-24T11:28:59.623Z", "2025-06-24T11:29:18.987Z"),
//                buildLog("1087", "beet", "2025-06-24T11:29:23.324Z", "2025-06-24T11:29:29.863Z")
//        );
//
//        List<Map<String, Object>> doctorLogReqList = Collections.singletonList(
//                buildLog("468", "doctor", "2025-06-24T11:29:36.734Z", "2025-06-24T11:29:43.929Z")
//        );
//
//        List<Map<String, Object>> clientFmcgLogReqList = Collections.singletonList(
//                buildLog("190", "client", "2025-06-24T11:29:55.455Z", "2025-06-24T11:30:03.178Z")
//        );
//
//        // --- Static Orders
//        List<Map<String, Object>> orderSynReqList = Arrays.asList(
//                buildOrder(1095, "1085", 9, 40, 2, "Vitamin D VTM_D_500mg", "Unit"),
//                buildOrder(111, "1087", 8, 40, 20, "Levothyroxine LVS_250mg", "Cases"),
//                buildClientOrder(1750764601551L, "190", 16, 40, 2, "Vitamin D VTM_D_200mg", "Unit")
//        );
//
//        // --- Static Samples
//        List<Map<String, Object>> sampleReqList = Arrays.asList(
//                buildSample(8, 2, 75, 40, 109, "1085", null, null),
//                buildSample(8, 2, 75, 40, null, null, 49, "468")
//        );
//
//        // --- Final Payload
//        payload.put("beetLogReqList", beetLogReqList);
//        payload.put("doctorLogReqList", doctorLogReqList);
//        payload.put("clientFmcgLogReqList", clientFmcgLogReqList);
//        payload.put("orderSynReqList", orderSynReqList);
//        payload.put("sampleReqList", sampleReqList);
//        payload.put("memberId", 75);
//        payload.put("doctorPlanId", 248);
//        payload.put("beetLogPlanId", 319);
//        payload.put("isStay", false);
//        payload.put("endYourDay", true);
//        payload.put("stay", false);
//
//        Map<String, Object> expense = new HashMap<>();
//        expense.put("memberId", 75);
//        expense.put("date", "2025-06-24");
//        expense.put("workingWith", "Self");
//        expense.put("vehicleOwnerId", 75);
//        expense.put("otherMemberIds", new ArrayList<>());
//        expense.put("modeOfTransport", "CAR");
//
//        payload.put("expenseWithTaAndDaReq", expense);
//        return payload;
//    }
//
//    private static Map<String, Object> buildLog(String id, String type, String checkIn, String checkOut) {
//        Map<String, Object> log = new HashMap<>();
//        log.put("latitude", 26.4448);
//        log.put("longitude", 80.3686);
//        log.put("checkIn", checkIn);
//        log.put("checkOut", checkOut);
//        log.put("remark", "");
//        log.put("remainder", false);
//        log.put("remainderDate", null);
//        log.put("status", "Completed");
//
//        switch (type) {
//            case "doctor" -> log.put("doctorLogId", id);
//            case "client" -> log.put("clientFmcgLogId", id);
//            default -> log.put("beetLogId", id);
//        }
//
//        return log;
//    }
//
//    private static Map<String, Object> buildOrder(int outletId, String beetLogId, int productId, int clientId,
//                                                  int qty, String productName, String bundleType) {
//        Map<String, Object> entry = new HashMap<>();
//        entry.put("outletId", outletId);
//
//        Map<String, Object> order = new HashMap<>();
//        order.put("productId", productId);
//        order.put("clientId", clientId);
//        order.put("salesLevel", "STOCKIST");
//        order.put("quantity", qty);
//        order.put("memberId", 75);
//        order.put("orderMedium", "OnCall");
//        order.put("productName", productName);
//        order.put("bundleType", bundleType);
//        order.put("beetId", 37);
//        order.put("outletId", outletId);
//        order.put("beetLogId", beetLogId);
//
//        entry.put("orderRequestList", Collections.singletonList(order));
//        return entry;
//    }
//
//    private static Map<String, Object> buildClientOrder(long outletId, String clientLogId, int productId, int clientId,
//                                                        int qty, String productName, String bundleType) {
//        Map<String, Object> entry = new HashMap<>();
//        entry.put("outletId", outletId);
//
//        Map<String, Object> order = new HashMap<>();
//        order.put("productId", productId);
//        order.put("clientId", clientId);
//        order.put("salesLevel", "WAREHOUSE");
//        order.put("quantity", qty);
//        order.put("memberId", 75);
//        order.put("orderMedium", "OnCall");
//        order.put("productName", productName);
//        order.put("bundleType", bundleType);
//        order.put("clientLogId", clientLogId);
//
//        entry.put("orderRequestList", Collections.singletonList(order));
//        return entry;
//    }
//
//    private static Map<String, Object> buildSample(int productId, int quantity, int memberId, int clientFmcgId,
//                                                   Integer outletId, String beetLogId, Integer doctorId, String clientLogId) {
//        Map<String, Object> sample = new HashMap<>();
//        sample.put("productId", productId);
//        sample.put("quantity", quantity);
//        sample.put("memberId", memberId);
//        sample.put("bundleType", "Unit");
//        sample.put("clientFmcgId", clientFmcgId);
//
//        if (outletId != null) sample.put("outletId", outletId);
//        if (beetLogId != null) sample.put("beetLogId", beetLogId);
//        if (doctorId != null) sample.put("doctorId", doctorId);
//        if (clientLogId != null) sample.put("clientLogId", clientLogId);
//
//        return sample;
//    }

}