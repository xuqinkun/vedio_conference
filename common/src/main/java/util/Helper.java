package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

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
}
