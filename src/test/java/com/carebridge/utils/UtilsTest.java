package com.carebridge.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class UtilsTest {

    @Test
    void getPropertyValueReturnsValueWhenPropertyExists() {
        String value = Utils.getPropertyValue("sample.key", "utils-test.properties");
        assertEquals("sample-value", value);
    }

    @Test
    void getPropertyValueReturnsNullWhenResourceIsMissing() {
        String value = Utils.getPropertyValue("sample.key", "missing-utils.properties");
        assertNull(value);
    }

    @Test
    void getObjectMapperDisablesFailOnUnknownProperties() {
        ObjectMapper mapper = new Utils().getObjectMapper();
        assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }

    @Test
    void convertToJsonMessageCreatesExpectedMap() {
        Map<String, String> payload = Utils.convertToJsonMessage("info", "hello");
        assertNotNull(payload);
        assertEquals("info", payload.get("type"));
        assertEquals("hello", payload.get("message"));
        assertEquals(2, payload.size());
    }
}
