package org.example.logger;

import org.example.logger.sink.LogMessageSink;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Logger {
    private static final Pattern MDC_PATTERN = Pattern.compile("\\{MDC:(\\w+)\\}");
    private final String loggerName;
    private final List<LogMessageSink> sinks = new ArrayList<>();
    private final DateTimeFormatter dateTimeFormat;
    //supported message keywords - TIMESTAMP, LEVEL, MESSAGE, LOGGER, MDC
    private final String messageFormat;
    private final String timeFormatPattern;

    // Async logging support
    private final boolean asyncMode;
    private final BlockingQueue<QueuedLogMessage> messageQueue;
    private final Thread workerThread;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final int bufferSize;
    private final ReentrantLock queueLock = new ReentrantLock();

    /**
     * Create a new synchronous logger
     */
    public Logger(String loggerName, String timeFormat, String messageFormat, LogMessageSink... sinks) {
        this(loggerName, timeFormat, messageFormat, false, 0, sinks);
    }

    /**
     * Create a new logger with optional async mode
     */
    public Logger(String loggerName, String timeFormat, String messageFormat,
                  boolean asyncMode, int bufferSize, LogMessageSink... sinks) {
        this.loggerName = loggerName;
        this.sinks.addAll(Arrays.asList(sinks));
        this.timeFormatPattern = timeFormat;
        this.dateTimeFormat = DateTimeFormatter.ofPattern(timeFormat);
        this.messageFormat = messageFormat;

        // Initialize async support if enabled
        this.asyncMode = asyncMode;
        this.bufferSize = bufferSize;

        if (asyncMode) {
            this.messageQueue = new LinkedBlockingQueue<>();
            this.workerThread = createAndStartWorkerThread();
        } else {
            this.messageQueue = null;
            this.workerThread = null;
        }

        // This will flush any remaining messages when application is shutting down
        Runtime.getRuntime().addShutdownHook(new Thread(this::flush));
    }

    // Create and start the worker thread for async processing
    private Thread createAndStartWorkerThread() {
        Thread worker = new Thread(() -> {
            while (running.get()) {
                try {
                    QueuedLogMessage message = messageQueue.take();
                    sendMessageToEachSink(message.getLevel(), message.getFormattedMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            // Process remaining messages on shutdown
            drainQueue();
        });
        worker.setName("Logger-Worker-" + loggerName);
        worker.setDaemon(true);
        worker.start();
        return worker;
    }

    private String formatMessage(Level messageLevel, String message) {
        String formattedTime = LocalDateTime.now().format(dateTimeFormat);
        String formattedMessage = messageFormat
                .replace("{TIMESTAMP}", formattedTime)
                .replace("{LEVEL}", messageLevel.name())
                .replace("{LOGGER}", loggerName);

        // Process MDC variables
        Matcher matcher = MDC_PATTERN.matcher(formattedMessage);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String mdcKey = matcher.group(1);
            String mdcValue = MDC.get(mdcKey);
            matcher.appendReplacement(sb, mdcValue != null ? Matcher.quoteReplacement(mdcValue) : "");
        }
        matcher.appendTail(sb);

        return sb.toString()
            .replace("{MESSAGE}", message);
    }

    public void log(Level messageLevel, String message) {
        if (messageLevel == null || message == null || message.isEmpty()) {
            return;
        }

        String formattedMessage = formatMessage(messageLevel, message);

        if (asyncMode) {
            queueMessage(messageLevel, formattedMessage);
        } else {
            sendMessageToEachSink(messageLevel, formattedMessage);
        }
    }

    private void queueMessage(Level messageLevel, String formattedMessage) {
        try {
            queueLock.lock();
            messageQueue.put(new QueuedLogMessage(messageLevel, formattedMessage));

            // Notify worker if buffer is full to promote processing
            if (messageQueue.size() >= bufferSize) {
                synchronized (workerThread) {
                    workerThread.notify();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Failed to queue log message: " + e.getMessage());
        } finally {
            queueLock.unlock();
        }
    }

    private void drainQueue() {
        if (messageQueue == null) return;

        QueuedLogMessage message;
        while ((message = messageQueue.poll()) != null) {
            sendMessageToEachSink(message.getLevel(), message.getFormattedMessage());
        }
    }

    /**
     * Logs a message with an exception stack trace
     *
     * @param messageLevel the log level
     * @param message      the message to log
     * @param throwable    the exception to include
     */
    public void log(Level messageLevel, String message, Throwable throwable) {
        if (messageLevel == null || message == null) {
            return;
        }

        if (throwable != null) {
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            message = message + "\n" + sw.toString();
        }

        log(messageLevel, message);
    }

    /**
     * Logs a DEBUG level message
     *
     * @param message the message to log
     */
    public void debug(String message) {
        log(Level.DEBUG, message);
    }

    /**
     * Logs a DEBUG level message with exception
     *
     * @param message   the message to log
     * @param throwable the exception to include
     */
    public void debug(String message, Throwable throwable) {
        log(Level.DEBUG, message, throwable);
    }

    /**
     * Logs an INFO level message
     *
     * @param message the message to log
     */
    public void info(String message) {
        log(Level.INFO, message);
    }

    /**
     * Logs an INFO level message with exception
     *
     * @param message   the message to log
     * @param throwable the exception to include
     */
    public void info(String message, Throwable throwable) {
        log(Level.INFO, message, throwable);
    }

    /**
     * Logs a WARN level message
     *
     * @param message the message to log
     */
    public void warn(String message) {
        log(Level.WARN, message);
    }

    /**
     * Logs a WARN level message with exception
     *
     * @param message   the message to log
     * @param throwable the exception to include
     */
    public void warn(String message, Throwable throwable) {
        log(Level.WARN, message, throwable);
    }

    /**
     * Logs an ERROR level message
     *
     * @param message the message to log
     */
    public void error(String message) {
        log(Level.ERROR, message);
    }

    /**
     * Logs an ERROR level message with exception
     *
     * @param message   the message to log
     * @param throwable the exception to include
     */
    public void error(String message, Throwable throwable) {
        log(Level.ERROR, message, throwable);
    }

    /**
     * Logs a FATAL level message
     *
     * @param message the message to log
     */
    public void fatal(String message) {
        log(Level.FATAL, message);
    }

    /**
     * Logs a FATAL level message with exception
     *
     * @param message   the message to log
     * @param throwable the exception to include
     */
    public void fatal(String message, Throwable throwable) {
        log(Level.FATAL, message, throwable);
    }

    private void sendMessageToEachSink(Level messageLevel, String formattedMessage) {
        for (LogMessageSink sink : sinks) {
            sink.consumeMessage(messageLevel, formattedMessage);
        }
    }

    public void flush() {
        if (asyncMode) {
            flushAsyncQueue();
        }
        sinks.forEach(LogMessageSink::flush);
    }

    private void flushAsyncQueue() {
        if (messageQueue == null) return;

        try {
            queueLock.lock();
            // Create a temporary thread to process all current messages
            Thread flushThread = new Thread(() -> {
                // Make a snapshot of the current queue to ensure we only process
                // messages that were in the queue at the time flush was called
                int currentSize = messageQueue.size();
                for (int i = 0; i < currentSize; i++) {
                    try {
                        QueuedLogMessage message = messageQueue.take();
                        sendMessageToEachSink(message.getLevel(), message.getFormattedMessage());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
            flushThread.start();
            flushThread.join(); // Wait for flush to complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            queueLock.unlock();
        }
    }

    /**
     * Stops the async worker thread and processes remaining messages
     */
    public void shutdown() {
        if (!asyncMode) return;

        running.set(false);
        if (workerThread != null) {
            workerThread.interrupt();
            try {
                workerThread.join(1000); // Wait up to 1 second for worker to complete
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Final flush to process any remaining messages
        drainQueue();
    }

    /**
     * Creates a new Logger with the same configuration but a different name.
     * Useful for creating loggers for different components.
     *
     * @param name the name for the new logger
     * @return a new Logger instance with the provided name
     */
    public Logger getLogger(String name) {
        return new Logger(name, timeFormatPattern, messageFormat,
                asyncMode, bufferSize,
                sinks.toArray(new LogMessageSink[0]));
    }

    /**
     * @return true if this logger is in async mode
     */
    public boolean isAsyncMode() {
        return asyncMode;
    }

    /**
     * @return the buffer size for async mode
     */
    public int getBufferSize() {
        return bufferSize;
    }

    // Wrapper class for queued log messages in async mode
    private static class QueuedLogMessage {
        private final Level level;
        private final String formattedMessage;
        private final long timestamp;

        QueuedLogMessage(Level level, String formattedMessage) {
            this.level = level;
            this.formattedMessage = formattedMessage;
            this.timestamp = System.nanoTime();
        }

        public Level getLevel() {
            return level;
        }

        public String getFormattedMessage() {
            return formattedMessage;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
