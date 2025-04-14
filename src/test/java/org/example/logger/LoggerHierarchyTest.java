package org.example.logger;

import org.example.logger.sink.LogMessageSink;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class LoggerHierarchyTest {

    private final String timeFormat = "yyyy-MM-dd HH:mm:ss";
    private final String messageFormat = "{TIMESTAMP} [{LEVEL}] [{LOGGER}] - {MESSAGE}";
    @Mock
    private LogMessageSink mockSink;
    @Captor
    private ArgumentCaptor<String> messageCaptor;
    private Logger rootLogger;

    @BeforeEach
    void setUp() {
        rootLogger = new Logger("root", timeFormat, messageFormat, mockSink);
    }

    @Test
    void childLoggerShouldShareSinks() {
        // Arrange
        Logger childLogger = rootLogger.getLogger("child");

        // Act
        childLogger.info("Child message");

        // Assert
        verify(mockSink).consumeMessage(eq(Level.INFO), messageCaptor.capture());
        String message = messageCaptor.getValue();
        assertTrue(message.contains("[child]"));
    }

    @Test
    void loggersShouldMaintainTheirNames() {
        // Arrange
        Logger childLogger = rootLogger.getLogger("child");
        Logger grandchildLogger = childLogger.getLogger("grandchild");

        // Act
        rootLogger.info("Root message");
        childLogger.info("Child message");
        grandchildLogger.info("Grandchild message");

        // Assert
        verify(mockSink, times(3)).consumeMessage(eq(Level.INFO), messageCaptor.capture());

        // Get all captured values - they're in order of capture
        var capturedValues = messageCaptor.getAllValues();

        assertEquals(3, capturedValues.size());
        assertTrue(capturedValues.get(0).contains("[root]"));
        assertTrue(capturedValues.get(1).contains("[child]"));
        assertTrue(capturedValues.get(2).contains("[grandchild]"));
    }

    @Test
    void loggersShouldShareFormatting() {
        // Arrange
        Logger childLogger = rootLogger.getLogger("child");

        // Act
        rootLogger.info("Root message");
        childLogger.info("Child message");

        // Assert
        verify(mockSink, times(2)).consumeMessage(eq(Level.INFO), messageCaptor.capture());

        var capturedValues = messageCaptor.getAllValues();

        // Both messages should have same timestamp format
        String timestampPattern = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}";
        assertTrue(capturedValues.get(0).matches(timestampPattern + ".*"));
        assertTrue(capturedValues.get(1).matches(timestampPattern + ".*"));
    }

    @Test
    void loggersShouldShareSinksForFlushing() {
        // Arrange
        Logger childLogger = rootLogger.getLogger("child");

        // Act
        childLogger.flush();

        // Assert
        verify(mockSink, times(1)).flush();
    }

    @Test
    void allLoggersInHierarchyShouldReceiveTheSameSink() {
        // Arrange
        Logger childLogger = rootLogger.getLogger("child");
        Logger siblingLogger = rootLogger.getLogger("sibling");

        // Act
        rootLogger.info("Root message");
        childLogger.info("Child message");
        siblingLogger.info("Sibling message");

        // Assert - all log messages should go to the same sink
        verify(mockSink, times(3)).consumeMessage(eq(Level.INFO), messageCaptor.capture());
    }
} 