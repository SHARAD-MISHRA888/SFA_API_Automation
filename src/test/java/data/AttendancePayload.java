package data;

import java.util.HashMap;
import java.util.Map;

public class AttendancePayload {

    public static Map<String, Object> buildAttendanceBody(int memberId, int reportingManagerId, String imageKey) {
        Map<String, Object> location = new HashMap<>();
        location.put("additionalProp1", new HashMap<>());
        location.put("additionalProp2", new HashMap<>());
        location.put("additionalProp3", new HashMap<>());

        Map<String, Object> body = new HashMap<>();
        body.put("memberId", memberId);
        body.put("reportingManager", reportingManagerId);
        body.put("doCheckIn", true);
        body.put("checkInLocation", location);
        body.put("imageKeyIn", imageKey);

        return body;
    }


    public static Map<String, Object> buildAttendanceBodyForCheckout(int memberId, int reportingManagerId, String imageKey) {
        Map<String, Object> location = new HashMap<>();
        location.put("additionalProp1", new HashMap<>());
        location.put("additionalProp2", new HashMap<>());
        location.put("additionalProp3", new HashMap<>());

        Map<String, Object> body = new HashMap<>();
        body.put("memberId", memberId);
        body.put("reportingManager", reportingManagerId);
        body.put("doCheckOut",true);
        body.put("checkOutLocation", location);
        body.put("imageKeyOut", imageKey);

        return body;
    }




}
