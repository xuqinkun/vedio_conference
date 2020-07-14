package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Helper {
    private static int imageWidth = 640*2;
    private static int imageHeight = 480*2;

    private final static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static int screenSizeWidth() {
        return imageWidth;
    }

    public static int screenSizeHeight() {
        return imageHeight;
    }

    public static String getUuid() {
        return UUID.randomUUID().toString();
    }

    public static String dateFormat(Date date) {
        return DATE_FORMATTER.format(date);
    }

    public static Date parseDate(String date) throws ParseException {
        return DATE_FORMATTER.parse(date);
    }
}
