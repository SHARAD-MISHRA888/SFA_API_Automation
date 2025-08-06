package tests;

import Utilities.*;
import base.BaseTest;
import data.AttendancePayload;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.util.Map;
import static Utilities.RestUtils.BASE64_IMAGE;
import static Utilities.RestUtils.SALESPERSON_TOKEN;

public class StartJourneyTest extends BaseTest {
    private static final String visitDate = "2025-08-05";
    @DataProvider(name = "memberDataProvider")
    public Object[][] memberDataProvider() {
        return new Object[][] {
                {22, 21},
                {25, 21}
        };
    }

    @Test(dataProvider = "memberDataProvider")
    public void startJourneyFlowForMember(int memberId, int reportingManagerId) {
        DataStore.clear(); // Ensure clean state for each member run
        System.out.println("==== Starting Journey for MemberID: " + memberId + " ====");

        // 1. Upload Image and Mark Attendance (Check-In)
        String imageKey = StartJourneyUtil.uploadAttendanceImage(BASE64_IMAGE, SALESPERSON_TOKEN);
        Map<String, Object> attendancePayload = AttendancePayload.buildAttendanceBody(memberId, reportingManagerId, imageKey);
        StartJourneyUtil.markAttendance(attendancePayload, SALESPERSON_TOKEN);
        DataStore.put("memberId", memberId);
        // 2. Update Vehicle Status
        String vehicleType = "CAR";
        StartJourneyUtil.updateVehicleType(SALESPERSON_TOKEN, memberId, vehicleType, visitDate);
        // 3. Fetch Combine Plan and Extract Logs
        CombinePlanExtractor.extractAllLogsAndPlanIds(SALESPERSON_TOKEN, memberId, visitDate);
        // 4. Generate Order and Sample Data
        AutoDataGenerator.generateOrderAndSampleData(SALESPERSON_TOKEN, memberId);
        // 5. Sync Logs and End Day (Handle Working/Non-Working Day)
        String workType = DataStore.get("workWith");
        if (!"Self".equalsIgnoreCase(workType) && !"Member".equalsIgnoreCase(workType)) {
            System.out.println("Non-Working Day detected for workType: " + workType);
            Response distanceUpdateResp = NonWorkingTypeUtil.syncNonWorkingLogs(SALESPERSON_TOKEN);
            distanceUpdateResp.prettyPrint();

            Response expenseResp = NonWorkingTypeUtil.expenseGenerateForNonWorkingType(SALESPERSON_TOKEN);
            expenseResp.prettyPrint();
        } else {
            System.out.println("Working Day for workType: " + workType);
            SyncMemberLogUtil.printPayloadForDebugging();
            Response response = SyncMemberLogUtil.syncLogsAndEndDay(SALESPERSON_TOKEN);
            SyncMemberLogUtil.updateWorkingWith(SALESPERSON_TOKEN);
            response.prettyPrint();
        }
        // 6. Mark Attendance (Check-Out)
        String punchOutImageKey = StartJourneyUtil.uploadAttendanceImage(BASE64_IMAGE, SALESPERSON_TOKEN);
        Map<String, Object> punchOutPayload = AttendancePayload.buildAttendanceBodyForCheckout(memberId, reportingManagerId, punchOutImageKey);
        StartJourneyUtil.markAttendance(punchOutPayload, SALESPERSON_TOKEN);
        System.out.println("==== Journey Completed for MemberID: " + memberId + " ====");
    }
}















