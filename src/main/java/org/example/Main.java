package org.example;

import org.example.logger.Level;
import org.example.logger.Logger;
import org.example.logger.LoggerConfig;
import org.example.logger.MDC;

import java.io.IOException;

/**
 * Demo application showing usage of the logging library
 */
public class Main {
    public static void main(String[] args) {
        try {
            // Create a logger with builder API
            Logger logger = createMainLogger();
            logger.info("Logging system initialized");

            // Basic logging demo
            demoBasicLogging(logger);

            // MDC demo
            demoMDCLogging(logger);

            // Exception logging demo
            demoExceptionLogging(logger);

            // Multi-threaded logging demo
            demoMultiThreadedLogging(logger);

            // Let async operations complete
            Thread.sleep(500);
            System.out.println("Application shutting down...");

        } catch (Exception e) {
            System.err.println("Error in logging demo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Logger createMainLogger() throws IOException {
        // Configuration using builder pattern
        return new LoggerConfig()
                .withName("MainLogger")
                .withDateFormat("yyyy-MM-dd HH:mm:ss")
                .withMessageFormat("{TIMESTAMP} [{LEVEL}] [{LOGGER}] [{MDC:requestId}] - {MESSAGE}")
                .withConsole(true, Level.DEBUG)  // Sync console
                .withFile(true, "logs/application.log", Level.INFO)  // File logging
                .build();
    }

    private static void demoBasicLogging(Logger logger) {
        logger.debug("Debug message");
        logger.info("Info message");
        logger.warn("Warning message");
        logger.error("Error message");
        logger.fatal("Fatal message");
    }

    private static void demoMDCLogging(Logger logger) {
        // Named loggers with MDC
        Logger userLogger = logger.getLogger("UserService");
        Logger orderLogger = logger.getLogger("OrderService");

        MDC.put("requestId", "USER-123");
        userLogger.info("User authenticated");

        MDC.put("requestId", "ORDER-456");
        orderLogger.info("Order processed");

        MDC.clear();
    }

    private static void demoExceptionLogging(Logger logger) {
        try {
            int result = 10 / 0; // Generate exception
        } catch (Exception e) {
            MDC.put("requestId", "ERROR-999");
            logger.error("Calculation error", e);
            MDC.clear();
        }
    }

    private static void demoMultiThreadedLogging(Logger logger) {
        for (int i = 0; i < 3; i++) {
            final int threadNum = i;
            new Thread(() -> {
                // Set MDC for this thread
                MDC.put("requestId", "THREAD-" + threadNum);
                logger.info("Thread-" + threadNum + " started");

                try {
                    Thread.sleep(100 * threadNum);
                } catch (InterruptedException e) {
                    logger.warn("Thread interrupted", e);
                }

                logger.info("Thread-" + threadNum + " finished");
                MDC.clear();
            }).start();
        }
    }
}
