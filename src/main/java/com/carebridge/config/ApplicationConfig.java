package com.carebridge.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.carebridge.utils.Utils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new Utils().getObjectMapper();
    }
}
