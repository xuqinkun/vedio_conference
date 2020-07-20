package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class Config {
    private static final Logger log = LoggerFactory.getLogger(Config.class);

    public static final String PORTRAIT_KEY = "portrait";
    public static final String DEFAULT_PORTRAIT_SRC = "/fxml/img/orange.png";
    public static final String CAPTURE_DEVICE_KEY = "capture.device";
    public static final String DEFAULT_CAPTURE_DEVICE = "0";
    public static final String KAFKA_CONSUMER_GROUP_ID_KEY = "kafka.consumer.groupID";
    public static final String DEFAULT_KAFKA_CONSUMER_GROUP_ID = "consumers";
    public static final String TRUSTED_PACKAGES_KEY = "trusted.packages";
    public static final String DEFAULT_TRUSTED_PACKAGES = "*";
    public static final String RECORDER_FRAMERATE_KEY = "recorder.framerate";
    public static final String DEFAULT_RECORDER_FRAMERATE = "30";
    public static final String CAPTURE_WIDTH_KEY = "capture.width";
    public static final String CAPTURE_HEIGHT_KEY = "capture.height";
    public static final int WEBCAM = 0;
    public static final int OPENCV_GRABBER = 1;
    public static final int FFMPEG_GRABBER = 2;
    public static final String SERVER_LOCAL_KEY = "server.local";
    public static final String DEFAULT_LOCAL_SERVER_HOSTNAME = "localhost";
    public static final String SERVER_REMOTE_KEY = "server.remote";
    public static final String DEFAULT_REMOTE_SERVER_HOSTNAME = "localhost";
    private static final String PORT_KEY = "server.port";
    private static final String DEFAULT_SERVER_PORT = "8080";
    private static final String NGINX_PORT_KEY = "nginx.port";
    private static final String DEFAULT_NGINX_PORT = "1935";
    public static final String KAFKA_SERVER_PORT_KEY = "kafka.port";
    public static final String DEFAULT_KAFKA_SERVER_PORT = "9092";
    public static final String HEARTBEATS_SERVER_PORT_KEY = "heartbeats.server.port";
    public static final String DEFAULT_HEARTBEATS_SERVER_PORT_VALUE = "8888";
    public static final String DEAULT_PROPERTIES_PATH = "app.properties";

    private Properties properties;

    private static final Config INSTANCE = new Config();

    private Config() {
        load(DEAULT_PROPERTIES_PATH);
    }

    public static Config getInstance() {
        return INSTANCE;
    }

    public void load(String src) {
        properties = new Properties();
        try {
            URL url = Config.class.getClassLoader().getResource(src);
            if (url != null) {
                properties.load(new FileInputStream(url.getPath()));
            } else {
                log.warn("File app.properties not found! Use default settings.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getServerPort() {
        return properties.getProperty(PORT_KEY, DEFAULT_SERVER_PORT);
    }

    public String getNginxPort() {
        return properties.getProperty(NGINX_PORT_KEY, DEFAULT_NGINX_PORT);
    }

    public String getDefaultPortraitSrc() {
        return properties.getProperty(PORTRAIT_KEY, DEFAULT_PORTRAIT_SRC);
    }

    public int getCaptureDevice() {
        String deviceID = properties.getProperty(CAPTURE_DEVICE_KEY, DEFAULT_CAPTURE_DEVICE);
        return Integer.parseInt(deviceID);
    }

    public String getNginxUrlPrefix() {
        return String.format("rtmp://%s:%s/live", getServerHost(), getNginxPort());
    }

    public String getNginxOutputStream(String meetingUUID, String username) {
        return String.format("%s/%s-%s", getNginxUrlPrefix(), meetingUUID, username);
    }

    private boolean useLocal = false;

    public boolean useLocalServer() {
        return useLocal;
    }

    public void setUseLocal(boolean useLocal) {
        this.useLocal = useLocal;
    }

    public String getServerHost() {
        if (useLocalServer()) {
            return properties.getProperty(SERVER_LOCAL_KEY, DEFAULT_LOCAL_SERVER_HOSTNAME);
        } else {
            return properties.getProperty(SERVER_REMOTE_KEY, DEFAULT_REMOTE_SERVER_HOSTNAME);
        }
    }

    public String getKafkaServerPort() {
        return properties.getProperty(KAFKA_SERVER_PORT_KEY, DEFAULT_KAFKA_SERVER_PORT);
    }

    public String getKafkaConsumerGroupID() {
        return properties.getProperty(KAFKA_CONSUMER_GROUP_ID_KEY, DEFAULT_KAFKA_CONSUMER_GROUP_ID);
    }

    public String getKafkaServer() {
        return String.format("%s:%s", getServerHost(), getKafkaServerPort());
    }

    public String[] getKafkaTrustedPackages() {
        String packages = properties.getProperty(TRUSTED_PACKAGES_KEY, DEFAULT_TRUSTED_PACKAGES);
        return packages.split(",");
    }

    public int getRecorderFrameRate() {
        String frameRate = properties.getProperty(RECORDER_FRAMERATE_KEY, DEFAULT_RECORDER_FRAMERATE);
        return Integer.parseInt(frameRate);
    }

    public int getCaptureImageWidth() {
        String width = properties.getProperty(CAPTURE_WIDTH_KEY, "640");
        return Integer.parseInt(width);
    }

    public int getCaptureImageHeight() {
        String width = properties.getProperty(CAPTURE_HEIGHT_KEY, "480");
        return Integer.parseInt(width);
    }

    public int getAudioSampleRate() {
        String sampleRate = properties.getProperty("audio.samplerate", "44100");
        return Integer.parseInt(sampleRate);
    }

    public int getAudioSampleSize() {
        String sampleRate = properties.getProperty("audio.samplesize", "16");
        return Integer.parseInt(sampleRate);
    }

    public int getAudioBitrate() {
        String sampleRate = properties.getProperty("audio.bitrate", "192000");
        return Integer.parseInt(sampleRate);
    }

    public int getAudioChannels() {
        String sampleRate = properties.getProperty("audio.channels", "2");
        return Integer.parseInt(sampleRate);
    }

    public String getVideoOutputStream(String meetingId, String username) {
        return getNginxOutputStream(meetingId, username) + "-video";
    }

    public String getAudioOutputStream(String meetingId, String username) {
        return getNginxOutputStream(meetingId, username) + "-audio";
    }

    public int getCaptureType() {
        String captureType = properties.getProperty("capture.type", String.valueOf(WEBCAM));
        return Integer.parseInt(captureType);
    }

    public String getHeartBeatsServerHost() {
        return getServerHost();
    }

    public int getHeartBeatsServerPort() {
        String port = properties.getProperty(HEARTBEATS_SERVER_PORT_KEY, DEFAULT_HEARTBEATS_SERVER_PORT_VALUE);
        return Integer.parseInt(port);
    }

    public int getHeartBeatsTimeout() {
        String timeout = properties.getProperty("heartbeats.timeout", "3");
        return Integer.parseInt(timeout);
    }
}
