package util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import common.bean.StateType;
import common.bean.User;
import common.bean.UserState;

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
        UserState aa = new UserState("aa", "123", StateType.RUNNING);
        byte [] data = JsonUtil.toByteArray(aa);
        UserState userState = byteArrayToObject(data, UserState.class);
        System.out.println(userState);
    }

    public static byte[] toByteArray(UserState userState) {
        return toJsonString(userState).getBytes();
    }

    public static UserState byteArrayToObject(byte[] data, Class<UserState> clazz) {
        return jsonToObject(new String(data), clazz);
    }
}
