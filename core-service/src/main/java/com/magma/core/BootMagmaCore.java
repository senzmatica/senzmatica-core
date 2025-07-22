package com.magma.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = {"com.magma.core"})
@EnableMongoRepositories(basePackages = {"com.magma.core"})
@EnableMongoAuditing
@EnableEurekaClient
@EnableDiscoveryClient
public class BootMagmaCore extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(BootMagmaCore.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(BootMagmaCore.class);
    }
}
