package com.carebridge.utils;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class Utils {

    public static String getPropertyValue(String propName, String resourceName) {
        try (InputStream is = Utils.class.getClassLoader().getResourceAsStream(resourceName)) {
            Properties prop = new Properties();
            prop.load(is);
            return prop.getProperty(propName);
        } catch (Exception ex) {
            return null;
        }
    }

    public ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.writer(new DefaultPrettyPrinter());
        return objectMapper;
    }

    public static Map<String, String> convertToJsonMessage(String type, String message) {
        return Map.of("type", type, "message", message);
    }
}
