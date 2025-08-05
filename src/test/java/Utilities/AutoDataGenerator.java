package Utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.*;

import static io.restassured.RestAssured.given;

public class AutoDataGenerator {

    public static void generateOrderAndSampleData(String token, int memberId) {

        DataStore.put("memberId", memberId);
        Response response = given()
                .header("Authorization", "Bearer " + token)
                .accept("application/hal+json")
                .get("https://staging.prism-sfa-dev.net/combine-tour-plan/getTodayCombinePlanByMemberId/" + memberId + "?visitDate=" + getTodayDate());

        List<Map<String, Object>> bjpList = response.jsonPath().getList("bjpReportResponseList");
        List<Map<String, Object>> djpList = response.jsonPath().getList("doctorReportResponseList");
        List<Map<String, Object>> cjpList = response.jsonPath().getList("cjpReportResponseList");


        Map<String, Object> firstDoctorLog = djpList.get(0);
        Integer doctorPlanId = (Integer) firstDoctorLog.get("planId");

        Map<String, Object> firstBjpLog = bjpList.get(0);
        Integer beetLogPlanId = (Integer) firstBjpLog.get("planId");


        DataStore.put("doctorPlanId",doctorPlanId);
        DataStore.put("beetLogPlanId",beetLogPlanId);


        List<Map<String, Object>> sampleInventory = fetchSampleInventory(memberId, token);
        List<Map<String, Object>> allProducts = fetchAllProducts(token);

        Map<Integer, List<Map<String, Object>>> productInventoryCache = new HashMap<>();
        Map<Integer, Integer> sampleInventoryMap = new HashMap<>();
        for (Map<String, Object> sample : sampleInventory) {
            Integer productId = (Integer) sample.get("productId");
            Integer quantity = (Integer) sample.get("sampleQuantity");
            sampleInventoryMap.put(productId, quantity);
        }

        List<Object> allOrders = new ArrayList<>();
        List<Map<String, Object>> allSamples = new ArrayList<>();

        // Add log-type markers
        if (bjpList != null) {
            for (Map<String, Object> log : bjpList) {
                log.put("isOutlet", true);
            }
        }
        if (djpList != null) {
            for (Map<String, Object> log : djpList) {
                log.put("isDoctor", true);
            }
        }
        if (cjpList != null) {
            for (Map<String, Object> log : cjpList) {
                log.put("isClient", true);
            }
        }

        List<Map<String, Object>> allLogs = new ArrayList<>();
        if (bjpList != null) allLogs.addAll(bjpList);
        if (djpList != null) allLogs.addAll(djpList);
        if (cjpList != null) allLogs.addAll(cjpList);
        System.out.println(allLogs);

        for (Map<String, Object> log : allLogs) {
            Map<String, Object> outletDto = log.containsKey("outletGetDto") ? (Map<String, Object>) log.get("outletGetDto") : null;
            Map<String, Object> doctorDto = log.containsKey("doctorRes") ? (Map<String, Object>) log.get("doctorRes") : null;
            Map<String, Object> clientDto = log.containsKey("clientFMCGResponse") ? (Map<String, Object>) log.get("clientFMCGResponse") : null;
            Map<String, Object> beetDto = log.containsKey("beet") ? (Map<String, Object>) log.get("beet") : null;
            String workWithDto = log.containsKey("workingWith") ? (String) log.get("workingWith") : null;

            DataStore.put("workWith",workWithDto);


            boolean isDoctor = log.containsKey("doctorRes");
            boolean isOutlet = log.containsKey("outletGetDto");
            boolean isClient = log.containsKey("clientFMCGResponse");

            Integer outletId = outletDto != null && outletDto.get("id") instanceof Integer ? (Integer) outletDto.get("id") : null;
            Integer clientId = clientDto != null && clientDto.get("id") instanceof Integer ? (Integer) clientDto.get("id") : outletDto != null && outletDto.containsKey("clientId") ? (Integer) outletDto.get("clientId") : null;
            Integer logId = log != null && log.containsKey("id") ? (Integer) log.get("id") : null;
//            Integer doctorLogId = isDoctor ? (Integer) log.get("id") : null;
//            Integer clientLogId = isClient ? (Integer) log.get("id") : null;
            if (isDoctor){
                DataStore.put("doctorLogId",logId);
            }
            if (isOutlet){
                DataStore.put("beetLogId",logId);
            }
            if (isClient){
                DataStore.put("clientLogId",logId);
            }





//            Map<String, Object> beetDto = log.containsKey("beet") ? (Map<String, Object>) log.get("beet") : null;
            Integer beetId = beetDto != null && beetDto.containsKey("id") ? (Integer) beetDto.get("id") : null;

         //  if (clientId == null) continue;   // Agar client null hoga tab vo next loop mein chala jayega aur is line ka code execute nahi hoga , Tabhi doctor ke liye nahi create ho rha hai

            // ----------------- BJP: Orders + Samples -----------------
            if ((isOutlet && workWithDto.equals("Self")) || workWithDto.equals("Member")) {
                if (!productInventoryCache.containsKey(clientId) && clientId != null) {
//                    System.out.println("Here is the client id........"+clientId);
                    List<Map<String, Object>> inventory = fetchProductInventory(clientId, token);
                    productInventoryCache.put(clientId, inventory);
                }

                List<Map<String, Object>> productInventory = productInventoryCache.get(clientId);

                Map<Integer, List<Map<String, Object>>> outletOrderMap = new HashMap<>();


                Set<Integer> outletIdLookUp = new HashSet<>();

                for (Map<String, Object> product : productInventory) {
                    Integer productId = (Integer) product.get("productId");
                    if (productId == null) continue;

                    Map<String, Object> order = new HashMap<>();
                    order.put("productId", productId);
                    order.put("quantity", 10);
                    order.put("bundleType", 1);
                    order.put("clientId", clientId);
                    order.put("beetId", beetId);
                    order.put("beetLogId", logId);
                    order.put("memberId", memberId);
                    order.put("orderMedium", "OnSite");
                    order.put("orderCallStatus", "Productive");
                    order.put("remarks", "Auto-generated BJP order");
                    order.put("discountCode", "");

                    if (isOutlet && outletId != null) {
                        order.put("salesLevel", "STOCKIST");
                        order.put("outletId", outletId);
                    } else {
                        order.put("salesLevel", "WAREHOUSE");
                    }

                    outletOrderMap.computeIfAbsent(outletId, k -> new ArrayList<>()).add(order);


                    for (Map.Entry<Integer, List<Map<String, Object>>> entry : outletOrderMap.entrySet()) {
                        if (outletIdLookUp.contains(entry.getKey())){
                            continue;
                        }
                        Map<String, Object> orderWrapper = new HashMap<>();
                        orderWrapper.put("outletId",entry.getKey());
                        orderWrapper.put("orderRequestList", entry.getValue());
                        allOrders.add(orderWrapper);
                        outletIdLookUp.add(entry.getKey());
                    }
                }

                for (Map<String, Object> sample : sampleInventory) {
                    Integer productId = (Integer) sample.get("productId");
                    if (productId == null) continue;

                    Integer available = sampleInventoryMap.getOrDefault(productId, 0);
                    if (available < 2) {
                        throw new RuntimeException("Not enough sample inventory for productId: " + productId);
                    }

                    Map<String, Object> sampleReq = new HashMap<>();
                    sampleReq.put("memberId", memberId);
                    sampleReq.put("quantity", 2);
                    sampleReq.put("productId", productId);
                    sampleReq.put("bundleType", 1);
                    sampleReq.put("beetLogId", logId);
                    sampleReq.put("clientFmcgId", clientId);

                    if (isOutlet && outletId != null) sampleReq.put("outletId", outletId);

                    allSamples.add(sampleReq);
                    sampleInventoryMap.put(productId, available - 2);
                }
            }

            // ----------------- DJP: Samples only -----------------
            if ((isDoctor && workWithDto.equals("Self")) || workWithDto.equals("Member")) {
                Integer doctorId = doctorDto != null && doctorDto.containsKey("id") ? (Integer) doctorDto.get("id") : null;
                if (doctorId != null) {
                    for (Map<String, Object> sample : sampleInventory) {
                        Integer productId = (Integer) sample.get("productId");
                        if (productId == null) continue;

                        Integer available = sampleInventoryMap.getOrDefault(productId, 0);
                        if (available < 2) {
                            throw new RuntimeException("Not enough sample inventory for productId: " + productId);
                        }

                        Map<String, Object> sampleReq = new HashMap<>();
                        sampleReq.put("memberId", memberId);
                        sampleReq.put("quantity", 2);
                        sampleReq.put("productId", productId);
                        sampleReq.put("bundleType", 1);
                        sampleReq.put("doctorLogId", logId);
                        sampleReq.put("clientFmcgId", clientId);

                        allSamples.add(sampleReq);
                        sampleInventoryMap.put(productId, available - 2);
                    }
                }
            }

            // ----------------- CJP: Orders (10 cases) + Samples -----------------
            if ((isClient && workWithDto.equals("Self")) || workWithDto.equals("Member")) {

                List<Map<String,Object>> orderList = new ArrayList<>();

                for (Map<String, Object> product : allProducts) {
                    Integer productId = product != null && product.containsKey("productId")?(Integer) product.get("productId") :null;
                    if (productId == null) continue;

//                    Map<Integer, List<Map<String, Object>>> outletOrderMap = new HashMap<>();

                    Map<String, Object> order = new HashMap<>();
                    order.put("productId", productId);
                    order.put("quantity", 10);
                    order.put("bundleType", "Cases");
                    order.put("clientId", clientId);
                    order.put("beetId", beetId);
                    order.put("clientLogId", logId);
                    order.put("memberId", memberId);
                    order.put("orderMedium", "OnSite");
                    order.put("orderCallStatus", "Productive");
                    order.put("remarks", "Auto-generated CJP order");
                    order.put("discountCode", "");
                    order.put("salesLevel", "WAREHOUSE");

                    orderList.add(order);

                }

                Map<String,Object> orderMap = new HashMap<>();
                orderMap.put("orderRequestList",orderList);
                allOrders.add(orderMap);


                for (Map<String, Object> sample : sampleInventory) {
                    Integer productId = (Integer) sample.get("productId");
                    if (productId == null) continue;

                    Integer available = sampleInventoryMap.getOrDefault(productId, 0);
                    if (available < 2) {
                        throw new RuntimeException("Not enough sample inventory for productId: " + productId);
                    }

                    Map<String, Object> sampleReq = new HashMap<>();
                    sampleReq.put("memberId", memberId);
                    sampleReq.put("quantity", 2);
                    sampleReq.put("productId", productId);
                    sampleReq.put("bundleType", "Cases");
                    sampleReq.put("clientLogId", logId);
                    sampleReq.put("clientFmcgId", clientId);

                    allSamples.add(sampleReq);
                    sampleInventoryMap.put(productId, available - 2);
                }
            }
        }

        DataStore.put("orders", allOrders);
        DataStore.put("samples", allSamples);

        System.out.println("Total Orders: " + allOrders.size());
        System.out.println("Total Samples: " + allSamples.size());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String ordersJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(allOrders);
            System.out.println("Orders in JSON Format:\n" + ordersJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("--------------------------------------------------");
        try {
            String ordersJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(allSamples);
            System.out.println("Sample in JSON Format:\n" + ordersJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


        private static List<Map<String, Object>> fetchSampleInventory (int memberId, String token){
            RestAssured.baseURI = "https://staging.prism-sfa-dev.net";
            Response response = given()
                    .header("Authorization", "Bearer " + token)
                    .get("/inventory-service/sample-inventory/getAllSampleMemberById/" + memberId +
                            "?page=0&pageSize=10&sortBy=createdDate&sortDirection=asc");

            // Debug response
            System.out.println("Sample Inventory Response: " + response.asString());

            // Adjust this if the response has a wrapper object like 'data'
            return response.jsonPath().getList("content"); // assuming pagination structure
        }


        private static List<Map<String, Object>> fetchProductInventory ( int clientId, String token){
            RestAssured.baseURI = "https://staging.prism-sfa-dev.net";

            Response response = given()
                    .header("Authorization", "Bearer " + token)
                    .get("/inventory-service/getAllInventoryByClinetFmcgId/" + clientId +
                            "?page=0&pageSize=10&sortBy=createdDate&sortDirection=desc");

            System.out.println("Product Inventory Response: " + response.asString());

            if (response.getStatusCode() != 200) {
                System.out.println("Failed to fetch product inventory. Status: " + response.getStatusCode());
                return Collections.emptyList();
            }

            // Safely extract the 'content' list from the response
            return response.jsonPath().getList("content");
        }


        public static List<Map<String, Object>> fetchAllProducts (String token){
            RestAssured.baseURI = "https://staging.prism-sfa-dev.net";

            Response response = given()
                    .header("Authorization", "Bearer " + token)
                    .header("accept", "application/hal+json")
                    .get("/product-service/products/all?page=0&pageSize=10&sortBy=createdDate&sortDirection=desc");

            System.out.println("Products Response: " + response.asString());

            if (response.getStatusCode() != 200) {
                System.out.println("Failed to fetch products. Status: " + response.getStatusCode());
                return Collections.emptyList();
            }

            // Assuming the response has a "content" array
            List<Map<String, Object>> productList = response.jsonPath().getList("content");

            if (productList == null || productList.isEmpty()) {
                System.out.println("No products found.");
                return Collections.emptyList();
            }

            System.out.println("Fetched " + productList.size() + " products.");
            return productList;
        }


        private static String getTodayDate () {
            return java.time.LocalDate.now().toString();
        }
    }













