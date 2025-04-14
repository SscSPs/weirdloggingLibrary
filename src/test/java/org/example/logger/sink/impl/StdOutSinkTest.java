package org.example.logger.sink.impl;

import org.example.logger.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StdOutSinkTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private StdOutSink sink;

    @BeforeEach
    void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        sink = new StdOutSink(Level.INFO);
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    void sinkShouldRespectLogLevelFiltering() {
        // Arrange
        String debugMessage = "Debug message";
        String infoMessage = "Info message";
        String errorMessage = "Error message";

        // Act
        sink.consumeMessage(Level.DEBUG, debugMessage);
        sink.consumeMessage(Level.INFO, infoMessage);
        sink.consumeMessage(Level.ERROR, errorMessage);
        sink.flush(); // Flush to ensure all messages are processed

        // Assert
        String output = outContent.toString();
        assertFalse(output.contains(debugMessage), "DEBUG message should be filtered out");
        assertTrue(output.contains(infoMessage), "INFO message should be included");
        assertTrue(output.contains(errorMessage), "ERROR message should be included");
    }

    @Test
    void sinkShouldPreserveMessageOrder() {
        // Arrange
        int messageCount = 50;

        // Act - Send messages with sequential identifiers
        for (int i = 0; i < messageCount; i++) {
            sink.consumeMessage(Level.INFO, "OrderedMessage-" + i);
        }
        sink.flush();

        // Assert
        String output = outContent.toString();
        List<Integer> messageOrder = extractMessageOrder(output, "OrderedMessage-(\\d+)");

        // Verify all messages are present
        assertEquals(messageCount, messageOrder.size(),
                "All messages should be output to stdout");

        // Check the order matches the sent order
        for (int i = 0; i < messageOrder.size(); i++) {
            assertEquals(i, messageOrder.get(i),
                    "Message at position " + i + " should have ID " + i + " but found " + messageOrder.get(i));
        }
    }

    @Test
    void sinkShouldMaintainOrderWithDifferentLevels() {
        // Arrange - Use different log levels in a predictable pattern
        int messageCount = 30;

        // Act - Send messages with alternating log levels
        for (int i = 0; i < messageCount; i++) {
            Level level = (i % 3 == 0) ? Level.INFO :
                    (i % 3 == 1) ? Level.WARN : Level.ERROR;
            sink.consumeMessage(level, "MixedLevelMessage-" + i);
        }
        sink.flush();

        // Assert
        String output = outContent.toString();
        List<Integer> messageOrder = extractMessageOrder(output, "MixedLevelMessage-(\\d+)");

        // Verify messages are properly filtered (DEBUG would be filtered) and in order
        // We expect all messages since none are DEBUG level
        assertEquals(messageCount, messageOrder.size(),
                "All messages above threshold should be present");

        for (int i = 0; i < messageOrder.size(); i++) {
            assertEquals(i, messageOrder.get(i),
                    "Message at position " + i + " should have ID " + i);
        }
    }

    /**
     * Extract message IDs from output string using a regex pattern
     */
    private List<Integer> extractMessageOrder(String output, String patternStr) {
        List<Integer> order = new ArrayList<>();
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(output);

        while (matcher.find()) {
            int messageId = Integer.parseInt(matcher.group(1));
            order.add(messageId);
        }

        return order;
    }
} 