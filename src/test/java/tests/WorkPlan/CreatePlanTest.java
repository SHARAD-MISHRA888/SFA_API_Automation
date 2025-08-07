package tests.WorkPlan;

import Utilities.PlanUtils;
import Utilities.RestUtils;
import base.BaseTest;
import lombok.extern.slf4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import data.Payload.Request.PlanPayloadData;
import org.testng.annotations.Test;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CreatePlanTest extends BaseTest {
    private static final Logger log = LoggerFactory.getLogger(CreatePlanTest.class);
    private final int memberId = 10;
    private final int clientFmcgId = 1 ;
    private Map<String, List<Integer>> planIds = new HashMap<>();

   @Test(priority = 1)
   public void createPlanWithDynamicPayload() throws JsonProcessingException {

       log.info("Here wa are creating plan with dynamic payload");
       Map<String, Object> payload = PlanPayloadData.getSmartDailyPlanPayload(memberId, clientFmcgId);

       ObjectMapper mapper = new ObjectMapper();
       mapper.registerModule(new JavaTimeModule());
       mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Optional for ISO-8601 format

       String jsonPayload = mapper.writeValueAsString(payload);
       System.out.println("Plan Creation Payload:\n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload));

       RestUtils.post("/combine-tour-plan/createCombineTourPlanInBulk", jsonPayload).then().statusCode(200);
   }


    @Test(priority = 2)
    public void fetchPlanIdsAfterCreation() {
       log.info("Found planId's between given start and end date");
        LocalDate startDate = LocalDate.of(2025, 8, 7);
        LocalDate endDate = LocalDate.of(2025, 8, 30);
        planIds = PlanUtils.getAllPlanIds(RestUtils.SALESPERSON_TOKEN,memberId, startDate, endDate);
        System.out.println("DJP Plan IDs: " + planIds.get("DJP"));
        System.out.println("CJP Plan IDs: " + planIds.get("CJP"));
        System.out.println("BJP Plan IDs: " + planIds.get("BJP"));
    }

   @Test(priority = 3, dependsOnMethods = "fetchPlanIdsAfterCreation")
    public void approveCreatedPlans() {

       log.info("Approve all plan from manager side using all planId's");
        if (planIds.isEmpty()) {
            throw new IllegalStateException("No plans found to approve. Please check previous steps.");
        }

        PlanUtils.approvePlans(planIds, RestUtils.SALESPERSON_TOKEN);
        System.out.println("All fetched plans approved.");
    }
}

