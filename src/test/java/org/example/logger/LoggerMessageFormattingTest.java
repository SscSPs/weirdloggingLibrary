package org.example.logger;

import org.example.logger.sink.LogMessageSink;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class LoggerMessageFormattingTest {

    private final String timeFormat = "yyyy-MM-dd HH:mm:ss";
    @Mock
    private LogMessageSink mockSink;
    @Captor
    private ArgumentCaptor<String> messageCaptor;
    private DateTimeFormatter dateTimeFormatter;

    @BeforeEach
    void setUp() {
        dateTimeFormatter = DateTimeFormatter.ofPattern(timeFormat);
    }

    @Test
    void shouldFormatMessageWithBasicPlaceholders() {
        // Arrange
        String format = "{TIMESTAMP} [{LEVEL}] [{LOGGER}] - {MESSAGE}";
        Logger logger = new Logger("TestLogger", timeFormat, format, mockSink);

        // Act
        logger.info("Test message");

        // Assert
        verify(mockSink).consumeMessage(eq(Level.INFO), messageCaptor.capture());
        String message = messageCaptor.getValue();

        // Check timestamp format
        assertTrue(message.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} .*"));
        assertTrue(message.contains("[INFO]"));
        assertTrue(message.contains("[TestLogger]"));
        assertTrue(message.contains("- Test message"));
    }

    @Test
    void shouldHandleCustomFormatWithDifferentOrder() {
        // Arrange
        String format = "{MESSAGE} - {LEVEL} - {LOGGER} at {TIMESTAMP}";
        Logger logger = new Logger("TestLogger", timeFormat, format, mockSink);

        // Act
        logger.debug("Debug message");

        // Assert
        verify(mockSink).consumeMessage(eq(Level.DEBUG), messageCaptor.capture());
        String message = messageCaptor.getValue();

        assertTrue(message.startsWith("Debug message - DEBUG - TestLogger at "));
    }

    @Test
    void shouldHandleRepeatedPlaceholders() {
        // Arrange
        String format = "{LEVEL}: {MESSAGE} (level={LEVEL})";
        Logger logger = new Logger("TestLogger", timeFormat, format, mockSink);

        // Act
        logger.warn("Warning message");

        // Assert
        verify(mockSink).consumeMessage(eq(Level.WARN), messageCaptor.capture());
        String message = messageCaptor.getValue();

        assertEquals("WARN: Warning message (level=WARN)", message);
    }

    @Test
    void shouldHandleCustomTimestampFormat() {
        // Arrange
        String customTimeFormat = "HH:mm:ss.SSS";
        String format = "{TIMESTAMP} {MESSAGE}";
        Logger logger = new Logger("TestLogger", customTimeFormat, format, mockSink);

        // Act
        logger.info("Test message");

        // Assert
        verify(mockSink).consumeMessage(eq(Level.INFO), messageCaptor.capture());
        String message = messageCaptor.getValue();

        // Pattern for time like "14:30:45.123"
        Pattern timePattern = Pattern.compile("\\d{2}:\\d{2}:\\d{2}\\.\\d{3} Test message");
        assertTrue(timePattern.matcher(message).matches());
    }

    @Test
    void shouldHandleMDCPlaceholders() {
        // Arrange
        String format = "{TIMESTAMP} {MDC:userId} {MDC:sessionId} {MESSAGE}";
        Logger logger = new Logger("TestLogger", timeFormat, format, mockSink);

        // Set MDC values
        MDC.put("userId", "user123");
        MDC.put("sessionId", "abc-xyz");

        try {
            // Act
            logger.info("User action");

            // Assert
            verify(mockSink).consumeMessage(eq(Level.INFO), messageCaptor.capture());
            String message = messageCaptor.getValue();

            assertTrue(message.contains("user123"));
            assertTrue(message.contains("abc-xyz"));
            assertTrue(message.contains("User action"));
        } finally {
            // Clean up
            MDC.clear();
        }
    }

    @Test
    void shouldHandleMixOfPlaceholdersAndLiteralText() {
        // Arrange
        String format = "App: {LOGGER} | Event: {MESSAGE} | Severity: {LEVEL} | Time: {TIMESTAMP}";
        Logger logger = new Logger("TestLogger", timeFormat, format, mockSink);

        // Act
        logger.error("System failure");

        // Assert
        verify(mockSink).consumeMessage(eq(Level.ERROR), messageCaptor.capture());
        String message = messageCaptor.getValue();

        assertTrue(message.contains("App: TestLogger"));
        assertTrue(message.contains("Event: System failure"));
        assertTrue(message.contains("Severity: ERROR"));
        assertTrue(message.contains("Time: "));
    }
} 