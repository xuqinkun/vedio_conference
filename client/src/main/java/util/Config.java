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
    public static final String KAFKA_SERVER_HOST_KEY = "kafka.server.host";
    public static final String DEFAULT_KAFKA_SERVER_HOST = "localhost";
    public static final String KAFKA_SERVER_PORT_KEY = "kafka.server.port";
    public static final String DEFAULT_KAFKA_SERVER_PORT = "9092";
    public static final String KAFKA_CONSUMER_GROUP_ID_KEY = "kafka.consumer.groupID";
    public static final String DEFAULT_KAFKA_CONSUMER_GROUP_ID = "consumers";
    public static final String TRUSTED_PACKAGES_KEY = "trusted.packages";
    public static final String DEFAULT_TRUSTED_PACKAGES = "*";
    public static final String RECORDER_FRAMERATE_KEY = "recorder.framerate";
    public static final String DEFAULT_RECORDER_FRAMERATE = "30";
    public static final String CAPTURE_WIDTH_KEY = "capture.width";
    public static final String CAPTURE_HEIGHT_KEY = "capture.height";

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

    public static String getKafkaServerHost() {
        return properties.getProperty(KAFKA_SERVER_HOST_KEY, DEFAULT_KAFKA_SERVER_HOST);
    }

    public static String getKafkaServerPort() {
        return properties.getProperty(KAFKA_SERVER_PORT_KEY, DEFAULT_KAFKA_SERVER_PORT);
    }

    public static String getKafkaConsumerGroupID() {
        return properties.getProperty(KAFKA_CONSUMER_GROUP_ID_KEY, DEFAULT_KAFKA_CONSUMER_GROUP_ID);
    }

    public static String getKafkaServer() {
        return String.format("%s:%s", getKafkaServerHost(), getKafkaServerPort());
    }

    public static String[] getKafkaTrustedPackages() {
        String packages = properties.getProperty(TRUSTED_PACKAGES_KEY, DEFAULT_TRUSTED_PACKAGES);
        return packages.split(",");
    }

    public static int getRecorderFrameRate() {
        String frameRate = properties.getProperty(RECORDER_FRAMERATE_KEY, DEFAULT_RECORDER_FRAMERATE);
        return Integer.parseInt(frameRate);
    }

    public static int getCaptureImageWidth() {
        String width = properties.getProperty(CAPTURE_WIDTH_KEY, "640");
        return Integer.parseInt(width);
    }

    public static int getCaptureImageHeight() {
        String width = properties.getProperty(CAPTURE_HEIGHT_KEY, "480");
        return Integer.parseInt(width);
    }

    public static int getAudioSampleRate() {
        String sampleRate = properties.getProperty("audio.samplerate", "44100");
        return Integer.parseInt(sampleRate);
    }

    public static int getAudioSampleSize() {
        String sampleRate = properties.getProperty("audio.samplesize", "16");
        return Integer.parseInt(sampleRate);
    }

    public static int getAudioBitrate() {
        String sampleRate = properties.getProperty("audio.bitrate", "192000");
        return Integer.parseInt(sampleRate);
    }

    public static int getAudioChannels() {
        String sampleRate = properties.getProperty("audio.channels", "2");
        return Integer.parseInt(sampleRate);
    }

    public static void main(String[] args) {
        System.out.println(getAudioChannels());
//        System.out.println(Arrays.toString(getKafkaTrustedPackages()));
    }
}
