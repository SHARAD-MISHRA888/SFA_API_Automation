package tests;
import Utilities.*;
import data.AttendancePayload;
import org.testng.annotations.Test;

import io.restassured.response.Response;

import java.util.Map;

public class StartJourneyTest {

    private static final String SALESPERSON_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJtb2JpbGUiOjk5MTg0MDE0MDcsImlkIjo3LCJ1c2VyUm9sZSI6WyJSZXBvcnRpbmdfTWFuYWdlciIsIk1hbmFnZXIiLCJDcmVhdGVfTWFuYWdlciIsIkVkaXRfTWFuYWdlciIsIkRlbGV0ZV9NYW5hZ2VyIiwiVmlld19NYW5hZ2VyIiwiQ2xpZW50Rk1DRyIsIkNsaWVudCIsIkNyZWF0ZV9CZWV0IiwiRWRpdF9CZWV0IiwiRGVsZXRlX0JlZXQiLCJWaWV3X0JlZXQiLCJDcmVhdGVfRXhwZW5zZSIsIkVkaXRfRXhwZW5zZSIsIkRlbGV0ZV9FeHBlbnNlIiwiVmlld19FeHBlbnNlIiwiQ3JlYXRlX0xlYXZlIiwiRWRpdF9MZWF2ZSIsIkRlbGV0ZV9MZWF2ZSIsIlZpZXdfTGVhdmUiLCJDcmVhdGVfT3V0bGV0IiwiRWRpdF9PdXRsZXQiLCJEZWxldGVfT3V0bGV0IiwiVmlld19PdXRsZXQiLCJDcmVhdGVfUHJvZHVjdCIsIkVkaXRfUHJvZHVjdCIsIkRlbGV0ZV9Qcm9kdWN0IiwiVmlld19Qcm9kdWN0Il0sInN1YiI6Ijk5MTg0MDE0MDciLCJpYXQiOjE3NTA3NDk0NTMsImV4cCI6MTc1MjkwOTQ1M30.pFKn0AJyyg8QL7_9g29DHCAVA-L1BomCI_foNPa80ZFnZQwCFdexepOprGG9EcylD3OyO2C-nFtXd5uPH6tvZw"; // truncated
    private static final int MEMBER_ID = 23;
    private static final int REPORTING_MANAGER_ID = 20;
    private static final String BASE64_IMAGE = "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";
   @Test(priority = 5)
    public void startJourneyFlow() {
        String imageKey = StartJourneyUtil.uploadAttendanceImage(BASE64_IMAGE, SALESPERSON_TOKEN);
        Map<String, Object> attendancePayload = AttendancePayload.buildAttendanceBody(MEMBER_ID, REPORTING_MANAGER_ID, imageKey);
        StartJourneyUtil.markAttendance(attendancePayload, SALESPERSON_TOKEN);
        DataStore.put("memberId", MEMBER_ID);
    }

   @Test(priority = 6)
    public void fetchCombinePlanAndExtractAllLogs() {
        String visitDate = "2025-07-08";
        CombinePlanExtractor.extractAllLogsAndPlanIds(SALESPERSON_TOKEN, MEMBER_ID, visitDate);
    }

   // @Test(priority = 7)
    public void getTodayTourPlanAndWorkType() {
        String visitDate = "2025-07-05";
        Map<String, Object> tourPlanDetails = PlanUtils.getTodayTourPlan(MEMBER_ID, visitDate, SALESPERSON_TOKEN);

        String workingWith = (String) tourPlanDetails.get("workingWith");
        Object beatIdObj = tourPlanDetails.get("beatId");


        System.out.println("Working With: " + workingWith);

        if (beatIdObj != null) {
            int beatId = (int) beatIdObj;
            DataStore.put("beatId", beatId);
            System.out.println("Beat ID from tour plan: " + beatId);
        } else {
            System.out.println("No Beat ID in tour plan. Trying to fetch from Combine Plan...");

            Object beetIdFromCombinePlan = DataStore.get("bjp_beet_id");
            if (beetIdFromCombinePlan != null) {
                int beetId = (int) beetIdFromCombinePlan;
                DataStore.put("beatId", beetId);
                System.out.println("Beat ID from Combine Plan: " + beetId);
            } else {
                System.out.println("Beat ID not found in Combine Plan either.");
            }
        }
    }


    @Test(priority = 8)
    public void generateOrderAndSampleData() {
        AutoDataGenerator.generateOrderAndSampleData(SALESPERSON_TOKEN, MEMBER_ID);
    }



  @Test(priority = 9)
    public void syncMemberLogsAndEndDay() {
        SyncMemberLogUtil.printPayloadForDebugging();

        Response response = SyncMemberLogUtil.syncLogsAndEndDay(SALESPERSON_TOKEN);
        response.prettyPrint();
    }

}





//package tests;
//
//import Utilities.*;
//import data.AttendancePayload;
//import io.restassured.response.Response;
//import org.testng.annotations.DataProvider;
//import org.testng.annotations.Test;
//
//import java.util.Map;
//
//public class StartJourneyTest {
//
//    private static final String SALESPERSON_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJtb2JpbGUiOjg4ODE3NzEwMDIsImlkIjoxLCJ1c2VyUm9sZSI6WyJDcmVhdGVfTWFuYWdlciIsIkVkaXRfTWFuYWdlciIsIkRlbGV0ZV9NYW5hZ2VyIiwiVmlld19NYW5hZ2VyIiwiQ3JlYXRlX0V4cGVuc2UiLCJFZGl0X0V4cGVuc2UiLCJEZWxldGVfRXhwZW5zZSIsIlZpZXdfRXhwZW5zZSIsIkNyZWF0ZV9MZWF2ZSIsIkVkaXRfTGVhdmUiLCJEZWxldGVfTGVhdmUiLCJWaWV3X0xlYXZlIiwiQ3JlYXRlX0JlZXQiLCJFZGl0X0JlZXQiLCJEZWxldGVfQmVldCIsIlZpZXdfQmVldCIsIkNyZWF0ZV9PdXRsZXQiLCJFZGl0X091dGxldCIsIkRlbGV0ZV9PdXRsZXQiLCJWaWV3X091dGxldCIsIkNyZWF0ZV9Qcm9kdWN0IiwiRWRpdF9Qcm9kdWN0IiwiRGVsZXRlX1Byb2R1Y3QiLCJWaWV3X1Byb2R1Y3QiLCJTdXBlcl9BZG1pbiIsIlJlcG9ydGluZ19NYW5hZ2VyIiwiTWFuYWdlciJdLCJzdWIiOiI4ODgxNzcxMDAyIiwiaWF0IjoxNzUxMzUyNDA4LCJleHAiOjE3NTM1MTI0MDh9.bnWmfjnmtPGMYp9nkqZjbtWKO3YI3Xt0CbXeC8pJYN6Nr_O35PHWXw_etybe9alk-pjZe7UndW1_mTDOqdZMUw"; // Keep secure
//    private static final String BASE64_IMAGE = "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"; // Keep compact for real use
//    private static final String VISIT_DATE = "2025-07-02"; // Common date for all members
//
//    //  DataProvider for multiple members
//    @DataProvider(name = "memberData")
//    public Object[][] memberData() {
//        return new Object[][]{
//               // {22, 21},
//                {25, 21}
//                // Add more {memberId, reportingManagerId} pairs here
//        };
//    }
//
//    //  Main Flow: All steps combined in one test
//    @Test(dataProvider = "memberData")
//    public void fullJourneyFlow(int memberId, int reportingManagerId) {
//        System.out.println("\n====================");
//        System.out.println("Running flow for MEMBER_ID: " + memberId + " | RM_ID: " + reportingManagerId);
//        System.out.println("====================");
//
//        DataStore.clear(); // Clear data before new run
//        DataStore.put("memberId", memberId);
//        DataStore.put("visitDate", VISIT_DATE);
//
//        // Step 1: Upload and Mark Attendance
//        String imageKey = StartJourneyUtil.uploadAttendanceImage(BASE64_IMAGE, SALESPERSON_TOKEN);
//        Map<String, Object> attendancePayload = AttendancePayload.buildAttendanceBody(memberId, reportingManagerId, imageKey);
//        StartJourneyUtil.markAttendance(attendancePayload, SALESPERSON_TOKEN);
//
//        // Step 2: Extract Combine Plan Logs
//        CombinePlanExtractor.extractAllLogsAndPlanIds(SALESPERSON_TOKEN, memberId, VISIT_DATE);
//
//        // Step 3: Get Today Tour Plan and Work Type
//        Map<String, Object> tourPlanDetails = PlanUtils.getTodayTourPlan(memberId, VISIT_DATE, SALESPERSON_TOKEN);
//        String workingWith = (String) tourPlanDetails.get("workingWith");
//        Object beatIdObj = tourPlanDetails.get("beatId");
//        System.out.println("Working With: " + workingWith);
//
//        if (beatIdObj != null) {
//            int beatId = (int) beatIdObj;
//            DataStore.put("beatId", beatId);
//            System.out.println("Beat ID: " + beatId);
//        } else {
//            System.out.println("No Beat ID found for today's tour plan.");
//        }
//
//        // Step 4: Generate Orders and Samples
//        AutoDataGenerator.generateOrderAndSampleData(SALESPERSON_TOKEN, memberId);
//
//        // Optional Debugging - Print Payload
//        SyncMemberLogUtil.printPayloadForDebugging();
//
//        // Step 5: Sync Logs and End Day
//        Response response = SyncMemberLogUtil.syncLogsAndEndDay(SALESPERSON_TOKEN);
//        response.prettyPrint();
//
//        System.out.println("Completed Journey for MEMBER_ID: " + memberId);
//    }
//}









