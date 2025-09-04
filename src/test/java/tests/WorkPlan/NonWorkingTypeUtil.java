package tests.WorkPlan;

import data.Payload.Response.DataStore;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.io.Console;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NonWorkingTypeUtil {

    public static Response syncNonWorkingLogs(String token) {
        // Build the payload
        Map<String, Object> payload = buildNonWorkingPayload();

        // Define the endpoint URL
        String endpoint = "https://staging.prism-sfa-dev.net/combine-tour-plan/updateBjpAndCjpAndDjpDistance"; // 🔁 Replace with actual PUT endpoint

        // Perform the PUT request using RestAssured
        Response response = RestAssured.given()
                .baseUri("https://staging.prism-sfa-dev.net/") // 🔁 Base URL
                .basePath("/combine-tour-plan/updateBjpAndCjpAndDjpDistance")    // 🔁 Path if needed separately
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


    public static Response expenseGenerateForNonWorkingType(String token) {
        // Build the payload
        Map<String, Object> payload = buildExpensePayloadForNonWorking();

        System.out.println("Non Working EMP Expense Payload  "+payload);

        // Define the endpoint URL
        String endpoint = "https://staging.prism-sfa-dev.net/createExpenseWithModeOfTravelAndDA"; // 🔁 Replace with actual PUT endpoint

        // Perform the PUT request using RestAssured
        Response response = RestAssured.given()
                .baseUri("https://staging.prism-sfa-dev.net/") // 🔁 Base URL
                .basePath("/createExpenseWithModeOfTravelAndDA")    // 🔁 Path if needed separately
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body(payload)
                .log().all()
                .when()
                .post() // PUT method
                .then()
                .log().all()
                .extract()
                .response();

        return response;
    }

    public static Map<String,Object> buildNonWorkingPayload(){

        Map<String, Object> payload = new HashMap<>();

       List<Integer> clientLogId = DataStore.get("clientLogIds");
       List<Integer> doctorLogId = DataStore.get("doctorLogIds");
       List<Integer> beatLogId = DataStore.get("beetLogIds");

        String workType = DataStore.get("workWith");
        payload.put("cjpId",clientLogId.get(0));
        payload.put("bjpId",beatLogId.get(0));
        payload.put("djpId",doctorLogId.get(0));
        payload.put("remark","Today's assigned work is completed");
        if (workType.equals("Transit")){
            payload.put("modeOfTransport","CAR");
            payload.put("distance",5000);
        }
        payload.put("beetJourneyPlanStatus","Completed");
        payload.put("doctorJourneyPlanStatus","Completed");
        payload.put("clientFmcgJourneyPlanStatus","Completed");

        return payload;

    }


    public static Map<String,Object> buildExpensePayloadForNonWorking(){

        Map<String, Object> payload = new HashMap<>();
        String workType = DataStore.get("WorkingWith");

        payload.put("memberId",DataStore.get("memberId"));
        payload.put("date", LocalDate.now().toString());
        payload.put("workingWith",workType);
        payload.put("vehicleOwnerId",DataStore.get("memberId"));
        payload.put("otherMemberIds", new ArrayList<>());
        payload.put("modeOfTransport","CAR");
        payload.put("adminStatus","ACCEPTED");
        payload.put("totalDistance",5000);

        return payload;
    }






}
