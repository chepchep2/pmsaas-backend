package com.chep.demo.todo.common.cursor;

import com.chep.demo.todo.exception.cursor.InvalidCursorTokenException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Map;

public class CursorTokenUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String encode(Map<String, Object> data) {
        try {
            byte[] json = mapper.writeValueAsBytes(data);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
        } catch (Exception e) {
            throw new InvalidCursorTokenException("Failed to encode cursor", e);
        }
    }

    public static Map<String, Object> decode(String token) {
        try {
            byte[] json = Base64.getUrlDecoder().decode(token);
            return mapper.readValue(json, new TypeReference<>(){});
        } catch (Exception e) {
            throw new InvalidCursorTokenException("Invalid or malformed cursor token", e);
        }
    }
}
