package tests.WorkPlan;

import Utilities.PlanUtils;
import Utilities.RestUtils;
import Utilities.DBUtility;
import base.BaseTest;
import config.MemberConfigLoader;
import endpoints.Endpoints;
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
//    private static final Logger log = LoggerFactory.getLogger(CreatePlanTest.class);
    private final Map<String, Map<String, List<Integer>>> planIdsPerMember = new HashMap<>();
    private static final String MEMBERS_CONFIG_PATH = "src/test/resources/members-config.json";
    private List<String> memberMobiles;


    @Test(priority = 1)
   public void createPlanWithDynamicPayload() throws JsonProcessingException {

       log.info("Creating plans for members from config: {}", MEMBERS_CONFIG_PATH);
        memberMobiles = MemberConfigLoader.loadMemberMobiles(MEMBERS_CONFIG_PATH);

       ObjectMapper mapper = new ObjectMapper();
       mapper.registerModule(new JavaTimeModule());
       mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Optional for ISO-8601 format

        for (String mobile : memberMobiles) {
            log.info("Creating plan for mobile: {}", mobile);

            Map<String, Object> payload = PlanPayloadData.getSmartDailyPlanPayload(mobile);
            String jsonPayload = mapper.writeValueAsString(payload);

            // Post and assert 200
            RestUtils.post(Endpoints.Create_Plan_In_Bulk, jsonPayload).then().statusCode(200);

            // optional logging
            log.info("Plan creation payload for {}:\n{}", mobile, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload));
        }
   }


    @Test(priority = 2,dependsOnMethods = "createPlanWithDynamicPayload")
    public void fetchPlanIdsAfterCreation() {
       log.info("Fetching plan IDs for created plans (per member).");
        LocalDate startDate = LocalDate.of(2025, 9, 11);
        LocalDate endDate = LocalDate.of(2025, 9, 30);
        for (String mobile : memberMobiles) {
            Integer memberId = PlanPayloadData.getMemberIdByMobile(mobile);

            Map<String, List<Integer>> planIds = PlanUtils.getAllPlanIds(RestUtils.SALESPERSON_TOKEN, memberId, startDate, endDate);
            planIdsPerMember.put(mobile, planIds);

            log.info("Member mobile: {} -> DJP: {}, CJP: {}, BJP: {}",
                    mobile,
                    planIds.get("DJP"),
                    planIds.get("CJP"),
                    planIds.get("BJP"));
        }

    }

    @Test(priority = 3, dependsOnMethods = "fetchPlanIdsAfterCreation")
    public void approveCreatedPlans() {
        log.info("Approving plans for each member using manager flow.");

        if (planIdsPerMember.isEmpty()) {
            throw new IllegalStateException("No plans found to approve. Did previous steps run?");
        }

        for (Map.Entry<String, Map<String, List<Integer>>> entry : planIdsPerMember.entrySet()) {
            String mobile = entry.getKey();
            Map<String, List<Integer>> planIds = entry.getValue();

            if (planIds == null || planIds.isEmpty()) {
                log.warn("No plan ids for mobile {} - skipping", mobile);
                continue;
            }

            log.info("Approving plans for mobile {}: {}", mobile, planIds);
            PlanUtils.approvePlans(planIds, RestUtils.SALESPERSON_TOKEN);
        }

        log.info("Approve step completed for all members.");
    }
}

