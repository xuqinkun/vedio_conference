package demo.kafka;

import org.junit.Test;
import util.Config;

public class ConfigTest {

    @Test
    public void test() {
        System.out.println(Config.getServerHost());
        System.out.println(Config.getServerPort());
        System.out.println(Config.getNginxUrlPrefix());
        System.out.println(Config.getKafkaServer());
    }
}
