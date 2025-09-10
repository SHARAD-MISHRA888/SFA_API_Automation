package data.Payload.Request;

import java.time.LocalDate;
import Utilities.DBUtility;
import java.time.LocalDateTime;
import java.util.*;

public class PlanPayloadData {

    public static Integer getMemberIdByMobile(String mobile) {
        return DBUtility.getSingleIntValue("SELECT id FROM sfa_db.members WHERE mobile = ?;", mobile);
    }



    public static Map<String, Object> getSmartDailyPlanPayload(String mobile) {

        Integer memberId = DBUtility.getSingleIntValue(
                "SELECT id FROM sfa_db.members WHERE mobile = ?;",
                mobile
        );
        Integer clientFmcgId = DBUtility.getSingleIntValue("SELECT id FROM sfa_db.clientfmcg where member_id =?;",memberId);
        List<Integer> beetIds = DBUtility.getIntList(
                "SELECT id FROM sfa_db.beet WHERE client_fmcg_id = ? AND approval_status = ?;",
                clientFmcgId,
                "Accepted");
        Integer reportingMangerId = DBUtility.getSingleIntValue("select reporting_manager_id From sfa_db.members where id = ?;",memberId);


        LocalDate today = LocalDate.of(2025,9,11);
        LocalDate endDate = LocalDate.of(2025, 9, 30);

        List<Map<String, Object>> doctorPlanList = new ArrayList<>();
        List<Map<String, Object>> clientFmcgPlanList = new ArrayList<>();


        List<String> workTypes = List.of("Self", "Admin_Work", "Member", "Meeting", "HO_Meeting", "Transit");
        System.out.println("beets"+beetIds);
        List<Integer> selfBeetIds =beetIds;
        int otherBeetId = 36;

        int index = 0;
        LocalDate current = today;


        while (!current.isAfter(endDate)) {
            List<LocalDateTime> localDateTimeList = new ArrayList<>();
            List<String> daysOfWeek = new ArrayList<>();
            String dayWeek = String.valueOf(current.getDayOfWeek());
            daysOfWeek.add(dayWeek);
            LocalDateTime dateStr = current.atStartOfDay();
            localDateTimeList.add(dateStr);
            String workType = workTypes.get(index % workTypes.size());

            int beetId;
            List<Integer> otherMemberIds = new ArrayList<>();

            if (workType.equals("Self") || workType.equals("Member")) {
                beetId = selfBeetIds.get(index % selfBeetIds.size()); // alternate between 1 and 38
                if (workType.equals("Member")) {
                    otherMemberIds = List.of(reportingMangerId); // only for Member
                }
            } else {
                beetId = otherBeetId; // for all other workTypes
            }

            // Client FMCG Plan block
            Map<String, Object> clientFmcgPlan = new HashMap<>();
            clientFmcgPlan.put("memberId", memberId);
            clientFmcgPlan.put("beetId", beetId);
            clientFmcgPlan.put("clientFmcgId", clientFmcgId);
            clientFmcgPlan.put("workingWith", workType);
            clientFmcgPlan.put("dateList",localDateTimeList);
            clientFmcgPlan.put("otherMemberIds", otherMemberIds);

            // Doctor Journey Plan block
            Map<String, Object> doctorPlan = new HashMap<>();
            doctorPlan.put("memberId", memberId);
            doctorPlan.put("startDate", dateStr);
            doctorPlan.put("endDate", dateStr);
            doctorPlan.put("daysOfWeek",daysOfWeek);
            doctorPlan.put("beetId", beetId);
            doctorPlan.put("workingWith", workType);
            doctorPlan.put("recurrenceType", "Daily");
            doctorPlan.put("otherMemberIds", otherMemberIds);
            doctorPlan.put("clientFmcgJourneyPlanReq", clientFmcgPlan);

            // Add to final lists
            doctorPlanList.add(doctorPlan);
            clientFmcgPlanList.add(clientFmcgPlan);

            // Move to next day
            index++;
            current = current.plusDays(1);
        }

        // Final Payload
        Map<String, Object> finalPayload = new HashMap<>();
        finalPayload.put("doctorJourneyPlanReqList", doctorPlanList);
        finalPayload.put("clientFmcgJourneyPlanReqList", clientFmcgPlanList);

        return finalPayload;
    }


}
