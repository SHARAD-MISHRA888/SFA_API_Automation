package tests;

import Utilities.PlanUtils;
import Utilities.RestUtils;
import base.BaseTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.PlanPayloadData;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreatePlanTest extends BaseTest {

    private final int memberId = 10;
    private final int clientFmcgId = 1 ;
    private Map<String, List<Integer>> planIds = new HashMap<>();

   @Test(priority = 1)
    public void createPlanWithDynamicPayload() throws JsonProcessingException {
        Map<String, Object> payload = PlanPayloadData.getSmartDailyPlanPayload(memberId, clientFmcgId);
        ObjectMapper mapper = new ObjectMapper();
        String jsonPayload = mapper.writeValueAsString(payload);
        System.out.println("Plan Creation Payload:\n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload));
        RestUtils.post("/combine-tour-plan/createCombineTourPlanInBulk",jsonPayload).then().statusCode(200);

    }

    @Test(priority = 2)
    public void fetchPlanIdsAfterCreation() {
        LocalDate startDate = LocalDate.of(2025, 8, 5);
        LocalDate endDate = LocalDate.of(2025, 8, 30);
        planIds = PlanUtils.getAllPlanIds(memberId, startDate, endDate);

        System.out.println("DJP Plan IDs: " + planIds.get("DJP"));
        System.out.println("CJP Plan IDs: " + planIds.get("CJP"));
        System.out.println("BJP Plan IDs: " + planIds.get("BJP"));
    }

   @Test(priority = 3, dependsOnMethods = "fetchPlanIdsAfterCreation")
    public void approveCreatedPlans() {
        if (planIds.isEmpty()) {
            throw new IllegalStateException("No plans found to approve. Please check previous steps.");
        }

        PlanUtils.approvePlans(planIds, RestUtils.SALESPERSON_TOKEN);
        System.out.println("All fetched plans approved.");
    }
}

