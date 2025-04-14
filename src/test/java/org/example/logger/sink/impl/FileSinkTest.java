package org.example.logger.sink.impl;

import org.example.logger.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileSinkTest {

    @TempDir
    Path tempDir;

    private Path logFile;
    private FileSink sink;

    @BeforeEach
    void setUp() throws IOException {
        logFile = tempDir.resolve("test.log");
        sink = new FileSink(Level.INFO, logFile.toString());
    }

    @AfterEach
    void tearDown() {
        if (sink != null) {
            sink.close();
        }
    }

    @Test
    void sinkShouldCreateLogFile() {
        // Assert
        assertTrue(Files.exists(logFile), "Log file should be created");
    }

    @Test
    void sinkShouldWriteMessagesToFile() throws IOException {
        // Arrange
        String testMessage = "Test log message";

        // Act
        sink.consumeMessage(Level.INFO, testMessage);
        sink.flush();

        // Assert
        List<String> lines = Files.readAllLines(logFile);
        assertEquals(1, lines.size());
        assertEquals(testMessage, lines.get(0));
    }

    @Test
    void sinkShouldRespectLogLevelFiltering() throws IOException {
        // Arrange
        String debugMessage = "Debug message";
        String infoMessage = "Info message";
        String errorMessage = "Error message";

        // Act
        sink.consumeMessage(Level.DEBUG, debugMessage);
        sink.consumeMessage(Level.INFO, infoMessage);
        sink.consumeMessage(Level.ERROR, errorMessage);
        sink.flush();

        // Assert
        List<String> lines = Files.readAllLines(logFile);
        assertEquals(2, lines.size());
        assertFalse(lines.contains(debugMessage), "DEBUG message should be filtered out");
        assertTrue(lines.contains(infoMessage), "INFO message should be included");
        assertTrue(lines.contains(errorMessage), "ERROR message should be included");
    }

    @Test
    void sinkShouldAppendToExistingFile() throws IOException {
        // Arrange
        String firstMessage = "First message";
        String secondMessage = "Second message";

        // Act - Write first message
        sink.consumeMessage(Level.INFO, firstMessage);
        sink.flush();

        // Act - Write second message
        sink.consumeMessage(Level.INFO, secondMessage);
        sink.flush();

        // Assert
        List<String> lines = Files.readAllLines(logFile);
        assertEquals(2, lines.size());
        assertEquals(firstMessage, lines.get(0));
        assertEquals(secondMessage, lines.get(1));
    }

    @Test
    void sinkShouldOverwriteExistingFileWhenAppendIsFalse() throws IOException {
        // Arrange
        String firstMessage = "First message";
        sink.consumeMessage(Level.INFO, firstMessage);
        sink.flush();
        sink.close();

        // Create a new sink with append=false
        sink = new FileSink(Level.INFO, logFile.toString(), false, true);
        String secondMessage = "Second message";

        // Act
        sink.consumeMessage(Level.INFO, secondMessage);
        sink.flush();

        // Assert
        List<String> lines = Files.readAllLines(logFile);
        assertEquals(1, lines.size(), "File should be overwritten");
        assertEquals(secondMessage, lines.get(0));
    }

    @Test
    void sinkShouldPreserveMessageOrder() throws IOException {
        // Arrange
        int messageCount = 50;

        // Act - Send messages with sequential identifiers
        for (int i = 0; i < messageCount; i++) {
            sink.consumeMessage(Level.INFO, "OrderedMessage-" + i);
        }
        sink.flush();

        // Assert
        List<String> lines = Files.readAllLines(logFile);
        List<Integer> messageOrder = extractMessageOrder(lines, "OrderedMessage-(\\d+)");

        // Verify all messages are present and in order
        assertEquals(messageCount, messageOrder.size(),
                "All messages should be written to the file");

        // Check the order matches the sent order
        for (int i = 0; i < messageOrder.size(); i++) {
            assertEquals(i, messageOrder.get(i),
                    "Message at position " + i + " should have ID " + i + " but found " + messageOrder.get(i));
        }
    }

    @Test
    void sinkShouldMaintainOrderAcrossFlushes() throws IOException {
        // Arrange
        int totalMessages = 30;
        int messagesPerFlush = 10;

        // Act - Send messages in batches with flushes between
        for (int batch = 0; batch < totalMessages / messagesPerFlush; batch++) {
            for (int i = 0; i < messagesPerFlush; i++) {
                int messageId = batch * messagesPerFlush + i;
                sink.consumeMessage(Level.INFO, "BatchMessage-" + messageId);
            }
            sink.flush();
        }

        // Assert
        List<String> lines = Files.readAllLines(logFile);
        List<Integer> messageOrder = extractMessageOrder(lines, "BatchMessage-(\\d+)");

        // Verify all messages are present
        assertEquals(totalMessages, messageOrder.size(),
                "All messages across all batches should be present");

        // Verify they are in the correct order
        for (int i = 0; i < totalMessages; i++) {
            assertEquals(i, messageOrder.get(i),
                    "Message at position " + i + " should have ID " + i);
        }
    }

    /**
     * Extract message IDs from log lines using a regex pattern
     */
    private List<Integer> extractMessageOrder(List<String> lines, String patternStr) {
        List<Integer> order = new ArrayList<>();
        Pattern pattern = Pattern.compile(patternStr);

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                int messageId = Integer.parseInt(matcher.group(1));
                order.add(messageId);
            }
        }

        return order;
    }
} 