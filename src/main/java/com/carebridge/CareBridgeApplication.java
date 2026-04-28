package com.carebridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.carebridge")
@EntityScan(basePackages = "com.carebridge.entities")
@EnableJpaRepositories(basePackages = "com.carebridge")
public class CareBridgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CareBridgeApplication.class, args);
    }
}
