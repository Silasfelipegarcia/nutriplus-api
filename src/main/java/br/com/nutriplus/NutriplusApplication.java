package br.com.nutriplus;

import br.com.nutriplus.infrastructure.config.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class NutriplusApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(NutriplusApplication.class);
        application.addListeners((ApplicationListener<ApplicationEnvironmentPreparedEvent>) event ->
                DotenvLoader.loadInto((ConfigurableEnvironment) event.getEnvironment()));
        application.run(args);
    }
}
