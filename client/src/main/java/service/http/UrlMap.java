package service.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Config;

public class UrlMap {

    private static final Logger log = LoggerFactory.getLogger(UrlMap.class);

    private static String baseUrl = String.format("http://%s:%s/", Config.getServerHost(), Config.getServerPort());

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
