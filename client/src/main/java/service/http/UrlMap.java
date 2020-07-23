package service.http;

import common.bean.OperationType;
import util.Config;

public class UrlMap {

    public static final Config config = Config.getInstance();

    private static String baseUrl = String.format("http://%s:%s/", config.getServerHost(), config.getServerPort());

    public static String getLoginUrl() {
        return baseUrl + "login";
    }

    public static String getRegisterUrl() {
        return baseUrl + "register";
    }

    public static String getCreateMeetingUrl() {
        return baseUrl + "createMeeting";
    }

    public static String getUserListUrl() {
        return baseUrl + "getUserList";
    }

    public static String getJoinMeetingUrl() {
        return baseUrl + "joinMeeting";
    }

    public static String getLeaveMeetingUrl() {
        return baseUrl + "leaveMeeting";
    }

    public static String getPermissionUrl(OperationType operationType) {
        return baseUrl + operationType.toString().toLowerCase();
    }
}
