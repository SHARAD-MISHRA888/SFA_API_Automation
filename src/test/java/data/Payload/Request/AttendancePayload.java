package data.Payload.Request;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.time.format.DateTimeFormatter;


public class AttendancePayload {


    public static Map<String, Object> buildAttendanceBody(int memberId, int reportingManagerId, String imageKey) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        LocalDateTime checkInTime = LocalDate.now().atTime(LocalTime.of(10, 0));
        String checkInTimestamp = checkInTime.format(formatter);

        Map<String, Object> checkInLocation = new HashMap<>();
        checkInLocation.put("latitude", 26.4448);
        checkInLocation.put("longitude", 80.3686);

        Map<String, Object> body = new HashMap<>();
        body.put("memberId", memberId);
        body.put("checkIn",checkInTimestamp);
        body.put("reportingManager", reportingManagerId);
        body.put("doCheckIn", true);
        body.put("checkInLocation", checkInLocation);
        body.put("imageKeyIn", imageKey);

        return body;
    }


    public static Map<String, Object> buildAttendanceBodyForCheckout(int memberId, int reportingManagerId, String imageKey) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        LocalDateTime checkOutTime = LocalDate.now().atTime(LocalTime.of(18, 0));
        String checkOutTimestamp = checkOutTime.format(formatter);

        Map<String, Object> checkOutLocation = new HashMap<>();
        checkOutLocation.put("latitude", 26.4448);
        checkOutLocation.put("longitude", 80.3686);

        Map<String, Object> body = new HashMap<>();
        body.put("checkOut", checkOutTimestamp);
        body.put("checkOutLocation", checkOutLocation);
        body.put("doCheckOut",true);
        body.put("imageKeyOut", imageKey);
        body.put("memberId", memberId);
        body.put("reportingManager", reportingManagerId);

        return body;
    }
}
