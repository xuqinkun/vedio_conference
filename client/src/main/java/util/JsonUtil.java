package util;

import com.google.gson.Gson;
import common.bean.Meeting;

import java.util.Date;

public class JsonUtil {
    private static Gson gson = new Gson();

    public static String toJsonString(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T jsonToObject(String jsonStr, Class<T> clazz) {
        return gson.fromJson(jsonStr, clazz);
    }

    public static void main(String[] args) {
//        HttpResult ret = new HttpResult(OK, "ss");
//        String json = JsonUtil.toJsonString(ret);
//        System.out.println(json);
//        System.out.println(jsonToObject(json, HttpResult.class));
        Date date = new Date();
        Meeting meeting = new Meeting("uuid", "password", "meetingType", date, date, true, false);
        String json = toJsonString(meeting);
        System.out.println(json);

//        System.out.println(toJsonString2("asda"));

    }
}
