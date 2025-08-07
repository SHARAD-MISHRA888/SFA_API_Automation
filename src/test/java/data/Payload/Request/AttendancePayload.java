package data.Payload.Request;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.time.format.DateTimeFormatter;


public class AttendancePayload {

    public static Map<String, Object> buildAttendanceBody(int memberId, int reportingManagerId, String imageKey) {
        Map<String, Object> checkInLocation = new HashMap<>();
        checkInLocation.put("latitude", 26.4448);
        checkInLocation.put("longitude", 80.3686);

        Map<String, Object> body = new HashMap<>();
        body.put("memberId", memberId);
        body.put("checkIn",LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")));
        body.put("reportingManager", reportingManagerId);
        body.put("doCheckIn", true);
        body.put("checkInLocation", checkInLocation);
        body.put("imageKeyIn", imageKey);

        return body;
    }


    public static Map<String, Object> buildAttendanceBodyForCheckout(int memberId, int reportingManagerId, String imageKey) {

        String checkInTimestamp = LocalDateTime.now().plusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"));

        Map<String, Object> checkInLocation = new HashMap<>();
        checkInLocation.put("latitude", 26.4448);
        checkInLocation.put("longitude", 80.3686);

        Map<String, Object> checkOutLocation = new HashMap<>();

        checkOutLocation.put("latitude", 26.4448);
        checkOutLocation.put("longitude", 80.3686);
        Map<String, Object> body = new HashMap<>();
        body.put("checkIn",LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")));
        body.put("checkOut", checkInTimestamp);
        body.put("checkOutLocation", checkOutLocation);
        body.put("checkIntLocation", checkInLocation);
        body.put("doCheckOut",true);
        body.put("doCheckIn",false);
        body.put("imageKeyOut", imageKey);
        body.put("imageKeyIn", imageKey);
        body.put("memberId", memberId);
        body.put("reportingManager", reportingManagerId);

        return body;
    }




}
