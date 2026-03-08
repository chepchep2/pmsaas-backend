package com.chep.demo.todo.common.cursor;

import com.chep.demo.todo.exception.cursor.InvalidCursorTokenException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class CursorTokenUtilsTest {

    @Test
    void decode_validToken_returnsDecodeMap() {
        String encoded = CursorTokenUtils.encode(
                Map.of("createdAt", "2026-03-08T16:43:41.169700Z", "id", 65L)
        );

        Map<String, Object> decoded = CursorTokenUtils.decode(encoded);

        assertThat(decoded.get("createdAt")).isEqualTo("2026-03-08T16:43:41.169700Z");
        assertThat(((Number) decoded.get("id")).longValue()).isEqualTo(65L);
    }

    @Test
    void decode_invalidToken_throwsInvalidCursorTokenException() {
        String invalidToken = "this-is-not-valid-base64-json!!";

        assertThatThrownBy(() -> CursorTokenUtils.decode(invalidToken))
                .isInstanceOf(InvalidCursorTokenException.class)
                .hasMessage("Invalid or malformed cursor token");
    }

    @Test
    void decode_nullToken_throwsInvalidCursorTokenException() {
        assertThatThrownBy(() -> CursorTokenUtils.decode(null))
                .isInstanceOf(InvalidCursorTokenException.class);
    }
}
