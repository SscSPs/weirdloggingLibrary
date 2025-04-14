package org.example.logger;

import org.example.logger.sink.LogMessageSink;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class LoggerMDCIntegrationTest {

    private final String timeFormat = "yyyy-MM-dd HH:mm:ss";
    private final String messageFormat = "{TIMESTAMP} [{LEVEL}] {MDC:userId} {MDC:requestId} - {MESSAGE}";
    @Mock
    private LogMessageSink mockSink;
    @Captor
    private ArgumentCaptor<String> messageCaptor;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = new Logger("MDCLogger", timeFormat, messageFormat, mockSink);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void logShouldIncludeMDCValues() {
        // Arrange
        MDC.put("userId", "user123");
        MDC.put("requestId", "req456");

        // Act
        logger.info("User logged in");

        // Assert
        verify(mockSink).consumeMessage(eq(Level.INFO), messageCaptor.capture());
        String formattedMessage = messageCaptor.getValue();
        assertTrue(formattedMessage.contains("user123"));
        assertTrue(formattedMessage.contains("req456"));
    }

    @Test
    void logShouldHandleMissingMDCValues() {
        // Arrange
        MDC.put("userId", "user123");
        // Intentionally not setting requestId

        // Act
        logger.info("User logged in");

        // Assert
        verify(mockSink).consumeMessage(eq(Level.INFO), messageCaptor.capture());
        String formattedMessage = messageCaptor.getValue();
        assertTrue(formattedMessage.contains("user123"));
        assertFalse(formattedMessage.contains("req456"));
    }

    @Test
    void logShouldUpdateWhenMDCChanges() {
        // Arrange
        MDC.put("userId", "user123");
        MDC.put("requestId", "req456");

        // Act
        logger.info("First message");

        // Change MDC value
        MDC.put("userId", "user999");

        logger.info("Second message");

        // Assert - use atLeastOnce() instead of default times(1)
        verify(mockSink, atLeastOnce()).consumeMessage(eq(Level.INFO), messageCaptor.capture());

        // Get all captured values
        var capturedValues = messageCaptor.getAllValues();

        // Check if any message contains the first user ID
        boolean hasFirstUser = capturedValues.stream()
                .anyMatch(msg -> msg.contains("user123"));
        assertTrue(hasFirstUser, "No message contains first user ID");

        // Check if any message contains the second user ID
        boolean hasSecondUser = capturedValues.stream()
                .anyMatch(msg -> msg.contains("user999"));
        assertTrue(hasSecondUser, "No message contains second user ID");
    }

    @Test
    void logShouldRespectMDCThreadLocality() throws Exception {
        // Arrange
        MDC.put("userId", "mainThreadUser");

        // Act - Run in separate thread with different MDC value
        Thread thread = new Thread(() -> {
            MDC.put("userId", "backgroundThreadUser");
            logger.info("Background thread message");
        });
        thread.start();
        thread.join();

        // Log from main thread
        logger.info("Main thread message");

        // Assert - use atLeastOnce() instead of default times(1)
        verify(mockSink, atLeastOnce()).consumeMessage(eq(Level.INFO), messageCaptor.capture());

        // Get all captured values
        var capturedValues = messageCaptor.getAllValues();

        // Check if any message contains the thread user ID
        boolean hasThreadUser = capturedValues.stream()
                .anyMatch(msg -> msg.contains("backgroundThreadUser"));
        assertTrue(hasThreadUser, "No message contains thread user ID");

        // Check if any message contains the main thread user ID
        boolean hasMainUser = capturedValues.stream()
                .anyMatch(msg -> msg.contains("mainThreadUser"));
        assertTrue(hasMainUser, "No message contains main thread user ID");
    }
} 