package service.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class UrlMap {
    private static String baseUrl;

    private static final Logger log = LoggerFactory.getLogger(UrlMap.class);

    public static final String DEFAULT_HOST = "localhost";

    public static final String HOST_KEY = "server.host";

    public static final String PORT_KEY = "server.port";

    public static final String DEFAULT_SERVER_PORT = "8080";

    static {
        Properties properties = new Properties();
        try {
            URL url = UrlMap.class.getClassLoader().getResource("app.properties");
            if (url != null) {
                properties.load(new FileInputStream(url.getPath()));
            } else {
                log.warn("File app.properties not found! Use default settings.");
            }
            String host = properties.getProperty(HOST_KEY) == null ?
                    DEFAULT_HOST : properties.getProperty(HOST_KEY);
            String port = properties.getProperty(PORT_KEY) == null ? DEFAULT_SERVER_PORT : properties.getProperty(PORT_KEY);
            baseUrl = String.format("http://%s:%s/", host, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    public static void main(String[] args) {
        System.out.println(getLoginUrl());
    }
}
