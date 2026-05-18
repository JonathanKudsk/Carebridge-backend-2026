package com.carebridge.utils.toon;

import com.carebridge.exceptions.ValidationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.toonformat.jtoon.JToon;
import io.javalin.http.Context;

import java.util.LinkedHashMap;
import java.util.Map;

public class ToonObjectMapper {

  public static final String CONTENT_TYPE = "application/toon";

  private final ObjectMapper objectMapper;

  public ToonObjectMapper() {
    this.objectMapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .registerModule(new JavaTimeModule());
  }

  public <T> T readValue(String body, Class<T> targetType) throws ValidationException {
    if (body == null || body.isBlank()) {
      throw new ValidationException("TOON body is required");
    }

    Object decoded;
    try {
      decoded = JToon.decode(body);
    } catch (RuntimeException e) {
      throw new ValidationException("Invalid TOON payload");
    }

    if (!(decoded instanceof Map<?, ?> decodedMap)) {
      throw new ValidationException("TOON payload must be an object");
    }

    Map<String, Object> normalized = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : decodedMap.entrySet()) {
      if (entry.getKey() == null) {
        continue;
      }
      normalized.put(String.valueOf(entry.getKey()), entry.getValue());
    }

    return objectMapper.convertValue(normalized, targetType);
  }

  public String writeValueAsString(Object value) {
    if (value == null) {
      return message("No content");
    }

    Map<String, Object> serialized = objectMapper.convertValue(value, new TypeReference<>() {
    });
    return JToon.encode(serialized);
  }

  public String message(String message) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("msg", message == null ? "" : message);
    return JToon.encode(payload);
  }

  public void write(Context ctx, int status, Object value) {
    ctx.status(status);
    ctx.contentType(CONTENT_TYPE);
    ctx.result(writeValueAsString(value));
  }

  public void writeMessage(Context ctx, int status, String message) {
    ctx.status(status);
    ctx.contentType(CONTENT_TYPE);
    ctx.result(message(message));
  }
}
