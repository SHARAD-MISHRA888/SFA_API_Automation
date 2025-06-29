package Utilities;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.*;

import static io.restassured.RestAssured.given;

public class AutoDataGenerator {

    public static void generateOrderAndSampleData(String token, int memberId) {
        Map<String, Object> fullResponse = PlanUtils.getTodayTourPlan(memberId, getTodayDate(), token);

        if (fullResponse == null || fullResponse.isEmpty()) {
            System.out.println("❌ Tour plan response is null or empty.");
            return;
        }

        Object dataObj = fullResponse.get("data");
        if (!(dataObj instanceof Map)) {
            System.out.println("❌ 'data' is missing or not a map.");
            return;
        }

        Map<String, Object> tourPlan = (Map<String, Object>) dataObj;

        Object clientIdObj = tourPlan.get("clientId");
        if (clientIdObj == null) {
            System.out.println("❌ clientId is missing in tour plan.");
            return;
        }
        int clientId = (int) clientIdObj;

        List<Map<String, Object>> outletList = (List<Map<String, Object>>) tourPlan.get("outlets");
        if (outletList == null || outletList.isEmpty()) {
            System.out.println("❌ No outlets found in today's tour plan.");
            return;
        }

        List<Map<String, Object>> sampleInventory = fetchSampleInventory(memberId, token);
        if (sampleInventory == null || sampleInventory.isEmpty()) {
            System.out.println("❌ No sample inventory found.");
            return;
        }

        Object sampleProductIdObj = sampleInventory.get(0).get("productId");
        if (sampleProductIdObj == null) {
            System.out.println("❌ sample productId is missing.");
            return;
        }
        int sampleProductId = (int) sampleProductIdObj;

        List<Map<String, Object>> productInventory = fetchProductInventory(clientId, token);
        if (productInventory == null || productInventory.isEmpty()) {
            System.out.println("❌ No product inventory found.");
            return;
        }

        Object orderProductIdObj = productInventory.get(0).get("productId");
        if (orderProductIdObj == null) {
            System.out.println("❌ order productId is missing.");
            return;
        }
        int orderProductId = (int) orderProductIdObj;

        List<Map<String, Object>> orders = new ArrayList<>();
        List<Map<String, Object>> samples = new ArrayList<>();

        for (Map<String, Object> outlet : outletList) {
            Object outletIdObj = outlet.get("id");
            if (outletIdObj == null) {
                System.out.println("⚠️ Skipping outlet with missing ID.");
                continue;
            }
            int outletId = (int) outletIdObj;

            // Create Order
            Map<String, Object> orderReq = new HashMap<>();
            orderReq.put("productId", orderProductId);
            orderReq.put("quantity", 10);
            orderReq.put("salesLevel", "WAREHOUSE");
            orderReq.put("clientId", clientId);
            orderReq.put("bundleType", "Cases");
            orderReq.put("memberId", memberId);
            orderReq.put("outletId", outletId);
            orderReq.put("orderMedium", "OnSite");
            orderReq.put("orderCallStatus", "Productive");
            orderReq.put("remarks", "Auto-generated order");
            orderReq.put("discountCode", "");

            Map<String, Object> outletOrderWrapper = new HashMap<>();
            outletOrderWrapper.put("outletId", outletId);
            outletOrderWrapper.put("orderRequestList", List.of(orderReq));
            orders.add(outletOrderWrapper);

            // Create Sample
            Map<String, Object> sampleReq = new HashMap<>();
            sampleReq.put("memberId", memberId);
            sampleReq.put("quantity", 2);
            sampleReq.put("clientFmcgId", clientId);
            sampleReq.put("outletId", outletId);
            sampleReq.put("bundleType", "Cases");
            sampleReq.put("productId", sampleProductId);
            samples.add(sampleReq);
        }

        DataStore.put("orders", orders);
        DataStore.put("samples", samples);

        System.out.println("✅ Auto-generated " + orders.size() + " orders and " + samples.size() + " samples.");
    }

    private static List<Map<String, Object>> fetchSampleInventory(int memberId, String token) {
        RestAssured.baseURI = "https://staging.prism-sfa-dev.net";
        Response response = given()
                .header("Authorization", "Bearer " + token)
                .get("/sample/getSampleInventoryForMember/" + memberId);
        return response.jsonPath().getList("");
    }

    private static List<Map<String, Object>> fetchProductInventory(int clientId, String token) {
        RestAssured.baseURI = "https://staging.prism-sfa-dev.net";
        Response response = given()
                .header("Authorization", "Bearer " + token)
                .get("/product/getProductInventoryByClientId/" + clientId);
        return response.jsonPath().getList("");
    }

    private static String getTodayDate() {
        return java.time.LocalDate.now().toString();
    }
}
