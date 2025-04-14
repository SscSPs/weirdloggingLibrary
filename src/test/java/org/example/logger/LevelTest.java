package org.example.logger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelTest {

    @Test
    void shouldHaveCorrectLevelValues() {
        assertEquals(0, Level.DEBUG.getLevelInt());
        assertEquals(1, Level.INFO.getLevelInt());
        assertEquals(2, Level.WARN.getLevelInt());
        assertEquals(3, Level.ERROR.getLevelInt());
        assertEquals(4, Level.FATAL.getLevelInt());
    }

    @Test
    void shouldMaintainCorrectOrder() {
        assertTrue(Level.DEBUG.getLevelInt() < Level.INFO.getLevelInt());
        assertTrue(Level.INFO.getLevelInt() < Level.WARN.getLevelInt());
        assertTrue(Level.WARN.getLevelInt() < Level.ERROR.getLevelInt());
        assertTrue(Level.ERROR.getLevelInt() < Level.FATAL.getLevelInt());
    }

    @Test
    void shouldConvertToString() {
        assertEquals("DEBUG", Level.DEBUG.toString());
        assertEquals("INFO", Level.INFO.toString());
        assertEquals("WARN", Level.WARN.toString());
        assertEquals("ERROR", Level.ERROR.toString());
        assertEquals("FATAL", Level.FATAL.toString());
    }

    @Test
    void shouldCompareUsingOrdinal() {
        // Verify ordinal matches expected order
        assertEquals(0, Level.DEBUG.ordinal());
        assertEquals(1, Level.INFO.ordinal());
        assertEquals(2, Level.WARN.ordinal());
        assertEquals(3, Level.ERROR.ordinal());
        assertEquals(4, Level.FATAL.ordinal());
    }

    @Test
    void shouldHaveSameNumberOfLevelsAsDefinedConstants() {
        // There should be 5 levels: DEBUG, INFO, WARN, ERROR, FATAL
        assertEquals(5, Level.values().length);
    }
} 