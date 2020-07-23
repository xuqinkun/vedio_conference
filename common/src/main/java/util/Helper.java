package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Helper {

    private final static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String getUuid() {
        String uuid = UUID.randomUUID().toString();
        return uuid.substring(uuid.lastIndexOf("-") + 1);
    }

    public static String currentDate() {
        return dateFormat(new Date());
    }

    public static String dateFormat(long timestamp) {
        return dateFormat(new Date(timestamp));
    }

    public static String dateFormat(Date date) {
        return DATE_FORMATTER.format(date);
    }

    public static Date parseDate(String date) throws ParseException {
        return DATE_FORMATTER.parse(date);
    }

    public static String millisToTime(long time) {
        int hour;
        int minute;
        int second;
        if (time <= 0) {
            return "00:00:00";
        } else {
            int millisOfHours = (int) TimeUnit.HOURS.toMillis(1);
            int millisOfMinutes = (int) TimeUnit.MINUTES.toMillis(1);
            int millisOfSeconds = (int) TimeUnit.SECONDS.toMillis(1);
            hour = (int) (time / millisOfHours);
            time = time % millisOfHours;
            minute = (int) (time / millisOfMinutes);
            time %= millisOfMinutes;
            second = (int) (time / millisOfSeconds);
            return timeFormat(hour) + ":" + timeFormat(minute) + ":" + timeFormat(second);
        }
    }

    public static String timeFormat(int num) {
        String retStr;
        if (num >= 0 && num < 10) {
            retStr = "0" + num;
        } else {
            retStr = "" + num;
        }
        return retStr;
    }
}
