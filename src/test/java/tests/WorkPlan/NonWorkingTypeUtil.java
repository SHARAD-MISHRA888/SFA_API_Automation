package tests.WorkPlan;

import data.Payload.Response.DataStore;
import endpoints.Endpoints;
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

        // Perform the PUT request using RestAssured
        Response response = RestAssured.given()
                .baseUri(Endpoints.BASE_URL) // Base URL
                .basePath(Endpoints.Update_Distance)    // Path if needed separately
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

        // Perform the PUT request using RestAssured
        Response response = RestAssured.given()
                .baseUri(Endpoints.BASE_URL) // 🔁 Base URL
                .basePath(Endpoints.Generate_Expense)    // 🔁 Path if needed separately
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
