package Utilities;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.*;

public class PlanDataInitializer {

    public static void initializeDataStoreFromFile(String filePath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> response = mapper.readValue(new File(filePath), new TypeReference<>() {});

            List<Map<String, Object>> bjpList = (List<Map<String, Object>>) response.get("bjpReportResponseList");
            if (bjpList == null || bjpList.isEmpty()) {
                System.out.println("⚠️ No BJP data found.");
                return;
            }

            Map<String, Object> bjp = bjpList.get(0); // Assuming 1 record
            int beetLogId = (int) bjp.get("id");
            int planId = (int) bjp.get("planId");
            int memberId = (int) ((Map<String, Object>) bjp.get("memberGetDto")).get("id");

            Map<String, Object> outlet = (Map<String, Object>) bjp.get("outletGetDto");
            int outletId = (int) outlet.get("id");
            String latitude = (String) outlet.get("latitude");
            String longitude = (String) outlet.get("longitude");
            int clientId = (int) outlet.get("clientId");

            // Put data in DataStore
            DataStore.put("beetLogIds", List.of(String.valueOf(beetLogId)));
            DataStore.put("beetLogPlanId", planId);
            DataStore.put("memberId", memberId);

            // Prepare one order sample entry (mock values for demo)
            Map<String, Object> order = new HashMap<>();
            order.put("productId", 8);
            order.put("clientId", clientId);
            order.put("salesLevel", "STOCKIST");
            order.put("quantity", 5);
            order.put("memberId", memberId);
            order.put("orderMedium", "OnCall");
            order.put("productName", "Sample Product");
            order.put("bundleType", "Unit");
            order.put("beetId", 2);
            order.put("outletId", outletId);
            order.put("beetLogId", beetLogId);

            Map<String, Object> orderGroup = new HashMap<>();
            orderGroup.put("outletId", outletId);
            orderGroup.put("orderRequestList", List.of(order));

            DataStore.put("orders", List.of(orderGroup));

            // Sample
            Map<String, Object> sample = new HashMap<>();
            sample.put("productId", 8);
            sample.put("quantity", 2);
            sample.put("memberId", memberId);
            sample.put("bundleType", "Unit");
            sample.put("clientFmcgId", clientId);
            sample.put("outletId", outletId);
            sample.put("beetLogId", beetLogId);
            DataStore.put("samples", List.of(sample));

            // For completeness (optional logs)
            System.out.println("✅ DataStore initialized from file.");
            System.out.println("🪪 beetLogId: " + beetLogId);
            System.out.println("👤 memberId: " + memberId);
            System.out.println("🏥 outletId: " + outletId);
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize data from file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
