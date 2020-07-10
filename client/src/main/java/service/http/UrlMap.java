package service.http;

public class UrlMap {
    private static String baseUrl = "http://localhost:8080/";

    public static String getLoginUrl() {
        return baseUrl + "login";
    }

    public static String getRegisterUrl() {
        return baseUrl + "register";
    }
}
