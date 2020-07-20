package demo.kafka;

import org.junit.Test;
import util.Config;

public class ConfigTest {

    @Test
    public void test() {
        Config config = Config.getInstance();
        System.out.println(config.getServerHost());
        System.out.println(config.getServerPort());
        System.out.println(config.getNginxUrlPrefix());
        System.out.println(config.getKafkaServer());
    }
}
