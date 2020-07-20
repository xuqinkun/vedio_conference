package util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import common.bean.HeartBeatsPacket;
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
        HeartBeatsPacket aa = new HeartBeatsPacket("aa", "123", UserState.RUNNING);
        byte [] data = JsonUtil.toByteArray(aa);
        HeartBeatsPacket heartBeatsPacket = byteArrayToObject(data, HeartBeatsPacket.class);
        System.out.println(heartBeatsPacket);
    }

    public static byte[] toByteArray(HeartBeatsPacket heartBeatsPacket) {
        return toJsonString(heartBeatsPacket).getBytes();
    }

    public static HeartBeatsPacket byteArrayToObject(byte[] data, Class<HeartBeatsPacket> clazz) {
        return jsonToObject(new String(data), clazz);
    }
}
