package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.http.UrlMap;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class Config {

    private static final Logger log = LoggerFactory.getLogger(Config.class);

    private static final String DEFAULT_HOST = "localhost";

    private static final String HOST_KEY = "server.host";

    private static final String PORT_KEY = "server.port";

    private static final String DEFAULT_SERVER_PORT = "8080";

    private static final String NGINX_HOST_KEY = "nginx.host";

    private static final String NGINX_PORT_KEY = "nginx.port";

    private static final String DEFAULT_NGINX_HOST = "localhost";

    private static final String DEFAULT_NGINX_PORT = "1935";
    public static final String PORTRAIT_KEY = "portrait";
    public static final String DEFAULT_PORTRAIT_SRC = "/fxml/img/orange.png";
    public static final String CAPTURE_DEVICE_KEY = "capture.device";
    public static final String DEFAULT_CAPTURE_DEVICE = "0";

    private static Properties properties;

    static {
        properties = new Properties();
        try {
            URL url = UrlMap.class.getClassLoader().getResource("app.properties");
            if (url != null) {
                properties.load(new FileInputStream(url.getPath()));
            } else {
                log.warn("File app.properties not found! Use default settings.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getServerHost() {
        return properties.getProperty(HOST_KEY, DEFAULT_HOST);
    }

    public static String getServerPort() {
        return properties.getProperty(PORT_KEY, DEFAULT_SERVER_PORT);
    }

    public static String getNginxHost() {
        return properties.getProperty(NGINX_HOST_KEY, DEFAULT_NGINX_HOST);
    }

    public static String getNginxPort() {
        return properties.getProperty(NGINX_PORT_KEY, DEFAULT_NGINX_PORT);
    }

    public static String getDefaultPortrait() {
        return properties.getProperty(PORTRAIT_KEY, DEFAULT_PORTRAIT_SRC);
    }

    public static int getCaptureDevice() {
        String deviceID = properties.getProperty(CAPTURE_DEVICE_KEY, DEFAULT_CAPTURE_DEVICE);
        return Integer.parseInt(deviceID);
    }

    public static String getNginxUrlPrefix() {
        return String.format("rtmp://%s:%s/live", getNginxHost(), getNginxPort());
    }

    public static String getNginxOutputStream(String meetingUUID, String username) {
        return String.format("%s/%s-%s", Config.getNginxUrlPrefix(), meetingUUID, username);
    }

    public static void main(String[] args) {
        System.out.println(getCaptureDevice());
    }
}
