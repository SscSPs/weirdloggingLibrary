package org.example.logger;

import org.example.logger.sink.LogMessageSink;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class LoggerTest {

    private final String timeFormat = "yyyy-MM-dd HH:mm:ss";
    private final String messageFormat = "{TIMESTAMP} [{LEVEL}] - {MESSAGE}";
    @Mock
    private LogMessageSink mockSink;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = new Logger("TestLogger", timeFormat, messageFormat, mockSink);
    }

    @Test
    void logShouldCallSinkWithFormattedMessage() {
        // Act
        logger.log(Level.INFO, "Test message");

        // Assert
        verify(mockSink, times(1)).consumeMessage(eq(Level.INFO), anyString());
    }

    @Test
    void logShouldNotCallSinkWithNullMessage() {
        // Act
        logger.log(Level.INFO, null);

        // Assert
        verify(mockSink, never()).consumeMessage(any(), anyString());
    }

    @Test
    void logShouldNotCallSinkWithEmptyMessage() {
        // Act
        logger.log(Level.INFO, "");

        // Assert
        verify(mockSink, never()).consumeMessage(any(), anyString());
    }

    @Test
    void logShouldNotCallSinkWithNullLevel() {
        // Act
        logger.log(null, "Test message");

        // Assert
        verify(mockSink, never()).consumeMessage(any(), anyString());
    }

    @Test
    void logExceptionShouldIncludeStackTrace() {
        // Arrange
        Exception testException = new RuntimeException("Test exception");

        // Act
        logger.log(Level.ERROR, "Error occurred", testException);

        // Assert
        verify(mockSink, times(1)).consumeMessage(eq(Level.ERROR), contains("Test exception"));
        verify(mockSink, times(1)).consumeMessage(eq(Level.ERROR), contains("RuntimeException"));
    }

    @Test
    void convenienceMethodsCallLogWithCorrectLevel() {
        // Act
        logger.debug("Debug message");
        logger.info("Info message");
        logger.warn("Warn message");
        logger.error("Error message");
        logger.fatal("Fatal message");

        // Assert
        verify(mockSink, times(1)).consumeMessage(eq(Level.DEBUG), contains("Debug message"));
        verify(mockSink, times(1)).consumeMessage(eq(Level.INFO), contains("Info message"));
        verify(mockSink, times(1)).consumeMessage(eq(Level.WARN), contains("Warn message"));
        verify(mockSink, times(1)).consumeMessage(eq(Level.ERROR), contains("Error message"));
        verify(mockSink, times(1)).consumeMessage(eq(Level.FATAL), contains("Fatal message"));
    }

    @Test
    void flushShouldCallFlushOnAllSinks() {
        // Act
        logger.flush();

        // Assert
        verify(mockSink, times(1)).flush();
    }
} 