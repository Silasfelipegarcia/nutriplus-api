package br.com.nutriplus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NutriplusApplication {

    public static void main(String[] args) {
        SpringApplication.run(NutriplusApplication.class, args);
    }
}
