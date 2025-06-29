package tests;

import Utilities.PlanUtils;
import Utilities.AuthUtils;
import data.PlanPayloadData;
import base.BaseTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreatePlanTest extends BaseTest {

    private String salespersonToken;
    private final int memberId = 9;
    private final int clientFmcgId = 2;
    private Map<String, List<Integer>> planIds = new HashMap<>();

    @Test(priority = 1)
    public void loginAsSalesperson() {
        salespersonToken = AuthUtils.login("9918401438", "Test@123");
        System.out.println("Logged in successfully. Token retrieved.");
    }

    @Test(priority = 2, dependsOnMethods = "loginAsSalesperson")
    public void createPlanWithDynamicPayload() throws JsonProcessingException {
        Map<String, Object> payload = PlanPayloadData.getSmartDailyPlanPayload(memberId, clientFmcgId);
        ObjectMapper mapper = new ObjectMapper();
        String jsonPayload = mapper.writeValueAsString(payload);

        System.out.println("Plan Creation Payload:\n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload));

        Response response = RestAssured.given()
                .baseUri("https://staging.prism-sfa-dev.net")
                .header("Authorization", "Bearer " + salespersonToken)
                .header("Accept", "application/hal+json")
                .header("Content-Type", "application/json")
                .body(jsonPayload)
                .post("/combine-tour-plan/createCombineTourPlanInBulk");

        response.then().statusCode(200);
        System.out.println("Plan Creation Response:\n" + response.asPrettyString());
    }

    @Test(priority = 3, dependsOnMethods = "createPlanWithDynamicPayload")
    public void fetchPlanIdsAfterCreation() {
        LocalDate startDate = LocalDate.of(2025, 6, 25);
        LocalDate endDate = LocalDate.of(2025, 6, 30);

        planIds = PlanUtils.getAllPlanIds(salespersonToken, memberId, startDate, endDate);

        System.out.println("DJP Plan IDs: " + planIds.get("DJP"));
        System.out.println("CJP Plan IDs: " + planIds.get("CJP"));
        System.out.println("BJP Plan IDs: " + planIds.get("BJP"));
    }

    @Test(priority = 4, dependsOnMethods = "fetchPlanIdsAfterCreation")
    public void approveCreatedPlans() {
        if (planIds.isEmpty()) {
            throw new IllegalStateException("No plans found to approve. Please check previous steps.");
        }

        PlanUtils.approvePlans(planIds, salespersonToken);
        System.out.println("All fetched plans approved.");
    }
}

