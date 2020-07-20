package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import service.HeartBeatsServer;
import util.Config;

import java.io.IOException;

@SpringBootApplication
@ComponentScan(basePackages = {"controller", "dao", "service", "configuration"})
public class Application {

    public static void main(String[] args) throws IOException {
        Config config = Config.getInstance();
        new HeartBeatsServer(config.getHeartBeatsServerPort()).start();
        SpringApplication.run(Application.class, args);
    }
}
