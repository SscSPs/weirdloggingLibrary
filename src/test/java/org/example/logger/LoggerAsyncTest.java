package org.example.logger;

import org.example.logger.sink.impl.StdOutSink;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Simple demonstration test of async Logger functionality.
 * This class uses a real sink for simplicity.
 */
public class LoggerAsyncTest {

    private final String timeFormat = "yyyy-MM-dd HH:mm:ss";
    private final String messageFormat = "{TIMESTAMP} [{LEVEL}] [{LOGGER}] - {MESSAGE}";
    private final int bufferSize = 10;
    private Logger asyncLogger;

    @BeforeEach
    void setUp() {
        // Create an async logger with a real stdout sink
        StdOutSink stdoutSink = new StdOutSink(Level.INFO);
        asyncLogger = new Logger("AsyncLogger", timeFormat, messageFormat, true, bufferSize, stdoutSink);
        System.out.println("==== TEST START ====");
    }

    @AfterEach
    void tearDown() {
        // Ensure async resources are properly released
        asyncLogger.shutdown();
        System.out.println("==== TEST END ====");
    }

    @Test
    void demonstrateAsyncLogging() throws InterruptedException {
        // Simply demonstrate that async logging works
        System.out.println("Sending log messages asynchronously...");

        // Send some test messages
        for (int i = 0; i < 5; i++) {
            asyncLogger.info("Async test message " + i);
        }

        // Flush messages and allow time to process
        asyncLogger.flush();
        System.out.println("Messages sent. Check console output for results.");

        // No assertions - this is just a demonstration that the code doesn't crash
    }
} 