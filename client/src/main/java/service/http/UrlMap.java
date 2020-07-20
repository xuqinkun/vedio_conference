package service.http;

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
}
