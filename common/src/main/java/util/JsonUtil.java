package util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import common.bean.User;

import java.util.ArrayList;
import java.util.List;

public class JsonUtil {
    private static Gson gson = new Gson();

    public static String toJsonString(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T jsonToObject(String jsonStr, Class<T> clazz) {
        return gson.fromJson(jsonStr, clazz);
    }

    public static <T> List<T> jsonToList(String jsonStr, Class<T> clazz) {
        JsonArray array = jsonToObject(jsonStr, JsonArray.class);
        List<T> list = new ArrayList<>();
        for (JsonElement next : array) {
            list.add((gson.fromJson(next, clazz)));
        }
        return list;
    }

    public static void main(String[] args) {
        ArrayList<Object> list = new ArrayList<>();
        list.add(new User("aaa", true));
        String jsonString = toJsonString(list);
        System.out.println(jsonString);
        System.out.println(jsonToList(jsonString, User.class));
    }
}
