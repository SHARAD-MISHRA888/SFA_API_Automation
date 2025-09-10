package tests.Journey;

import Utilities.DBUtility;
import config.MemberConfigLoader;
import base.BaseTest;
import data.Payload.Request.AttendancePayload;
import data.Payload.Request.AutoDataGenerator;
import data.Payload.Response.DataStore;
import io.restassured.response.Response;
import lombok.extern.slf4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tests.Attendance.AttendanceAndVehicle;
import tests.Sync.SyncMemberLogUtil;
import tests.WorkPlan.CombinePlanExtractor;
import tests.WorkPlan.NonWorkingTypeUtil;

import java.util.List;
import java.util.Map;
import static Utilities.RestUtils.BASE64_IMAGE;
import static Utilities.RestUtils.SALESPERSON_TOKEN;
@Slf4j
public class StartJourneyTest extends BaseTest {

    private static final String visitDate = "2025-09-10";

    private static final String MEMBERS_CONFIG_PATH = "src/test/resources/members-config.json";
//    private static final Logger log = LoggerFactory.getLogger(StartJourneyTest.class);

    @DataProvider(name = "memberDataProvider")
    public Object[][] memberDataProvider() {
        log.info("Under Data-Provider method--MEMBER_ID,REPORTING_MANAGER_ID");
        List<String> mobiles = MemberConfigLoader.loadMemberMobiles(MEMBERS_CONFIG_PATH);

        // Step 2: Prepare data array
        Object[][] data = new Object[mobiles.size()][2];

        // Step 3: Loop mobiles and fetch IDs from DB
        for (int i = 0; i < mobiles.size(); i++) {
            String mobile = mobiles.get(i);
            Integer memberId = DBUtility.getMemberIdByMobile(mobile);
            Integer managerId = DBUtility.getManagerIdByMemberId(memberId);

            data[i][0] = memberId;
            data[i][1] = managerId;

            log.info("Mobile {} -> MemberId {}, ManagerId {}", mobile, memberId, managerId);
        }

        return data;
    }

    @Test(dataProvider = "memberDataProvider")
    public void startJourneyFlowForMember(int memberId, int reportingManagerId) throws InterruptedException {
        log.info("Start and complete journey for today for salesperson");
        DataStore.clear(); // Ensure clean state for each member run
        System.out.println("==== Starting Journey for MemberID: " + memberId + " ====");

        log.info("Upload Image and Mark Attendance (Check-In)");
        String imageKey = AttendanceAndVehicle.uploadAttendanceImage(BASE64_IMAGE, SALESPERSON_TOKEN);
        Map<String, Object> attendancePayload = AttendancePayload.buildAttendanceBody(memberId, reportingManagerId, imageKey);
        AttendanceAndVehicle.markAttendance(attendancePayload, SALESPERSON_TOKEN);
        DataStore.put("memberId", memberId);
        log.info("Update Vehicle Status");
        String vehicleType = "CAR";
        AttendanceAndVehicle.updateVehicleType(SALESPERSON_TOKEN, memberId, vehicleType, visitDate);
        log.info("Fetch Combine Plan and Extract Logs");
        CombinePlanExtractor.extractAllLogsAndPlanIds(SALESPERSON_TOKEN, memberId, visitDate);
        log.info("Generate Order and Sample Data");
        AutoDataGenerator.generateOrderAndSampleData(SALESPERSON_TOKEN, memberId);
        Thread.sleep(2000);
        log.info("Sync Logs and End Day (Handle Working/Non-Working Day)");
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
            Thread.sleep(2000);
            Response response = SyncMemberLogUtil.syncLogsAndEndDay(SALESPERSON_TOKEN);
            SyncMemberLogUtil.updateWorkingWith(SALESPERSON_TOKEN);
            response.prettyPrint();
        }
        log.info("Mark Attendance (Check-Out)");
       String punchOutImageKey = AttendanceAndVehicle.uploadAttendanceImage(BASE64_IMAGE, SALESPERSON_TOKEN);
       Map<String, Object> punchOutPayload = AttendancePayload.buildAttendanceBodyForCheckout(memberId, reportingManagerId, punchOutImageKey);
       AttendanceAndVehicle.markAttendance(punchOutPayload, SALESPERSON_TOKEN);
       System.out.println("==== Journey Completed for MemberID: " + memberId + " ====");
    }
}















