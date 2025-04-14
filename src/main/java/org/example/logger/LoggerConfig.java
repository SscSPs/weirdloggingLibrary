package org.example.logger;

import org.example.logger.sink.LogMessageSink;
import org.example.logger.sink.impl.FileSink;
import org.example.logger.sink.impl.StdOutSink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Configuration class for the logging system using a fluent builder pattern.
 */
public class LoggerConfig {
    private final List<LogMessageSink> sinks = new ArrayList<>();
    // Default configuration values
    private String loggerName = "RootLogger";
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";
    private String messageFormat = "{TIMESTAMP} [{LEVEL}] [{LOGGER}] - {MESSAGE}";
    private boolean includeConsole = true;
    private boolean includeFile = false;
    private String logFilePath = "logs/application.log";
    private Level consoleLevel = Level.INFO;
    private Level fileLevel = Level.INFO;
    private int asyncBufferSize = 100;
    private boolean appendToFile = true;
    private boolean immediateFlush = true;
    private boolean asyncMode = false;

    /**
     * Load a logger configuration from properties.
     */
    public static LoggerConfig fromProperties(Properties props) {
        LoggerConfig config = new LoggerConfig();

        // Apply properties if they exist
        if (props.containsKey("logger.name")) {
            config.withName(props.getProperty("logger.name"));
        }

        if (props.containsKey("logger.dateFormat")) {
            config.withDateFormat(props.getProperty("logger.dateFormat"));
        }

        if (props.containsKey("logger.messageFormat")) {
            config.withMessageFormat(props.getProperty("logger.messageFormat"));
        }

        // Configure console output
        if (props.containsKey("logger.console.enabled")) {
            boolean enabled = Boolean.parseBoolean(props.getProperty("logger.console.enabled"));
            Level level = props.containsKey("logger.console.level")
                    ? Level.valueOf(props.getProperty("logger.console.level"))
                    : Level.INFO;

            config.withConsole(enabled, level);
        }

        // Configure file output
        if (props.containsKey("logger.file.enabled")) {
            boolean enabled = Boolean.parseBoolean(props.getProperty("logger.file.enabled"));
            String path = props.getProperty("logger.file.path", "logs/application.log");
            Level level = props.containsKey("logger.file.level")
                    ? Level.valueOf(props.getProperty("logger.file.level"))
                    : Level.INFO;

            config.withFile(enabled, path, level,
                    Boolean.parseBoolean(props.getProperty("logger.file.append", "true")),
                    Boolean.parseBoolean(props.getProperty("logger.file.immediateFlush", "true")));
        }

        // Configure async mode
        if (props.containsKey("logger.async.enabled")) {
            config.withAsyncMode(Boolean.parseBoolean(props.getProperty("logger.async.enabled")));
        }

        if (props.containsKey("logger.async.bufferSize")) {
            config.withAsyncBufferSize(
                    Integer.parseInt(props.getProperty("logger.async.bufferSize")));
        }

        return config;
    }

    // Builder methods
    public LoggerConfig withName(String name) {
        this.loggerName = name;
        return this;
    }

    public LoggerConfig withDateFormat(String format) {
        this.dateFormat = format;
        return this;
    }

    public LoggerConfig withMessageFormat(String format) {
        this.messageFormat = format;
        return this;
    }

    public LoggerConfig withSink(LogMessageSink sink) {
        this.sinks.add(sink);
        return this;
    }

    public LoggerConfig withConsole(boolean enabled) {
        this.includeConsole = enabled;
        return this;
    }

    public LoggerConfig withConsole(boolean enabled, Level level) {
        this.includeConsole = enabled;
        this.consoleLevel = level;
        return this;
    }

    public LoggerConfig withFile(boolean enabled, String filePath) {
        this.includeFile = enabled;
        this.logFilePath = filePath;
        return this;
    }

    public LoggerConfig withFile(boolean enabled, String filePath, Level level) {
        this.includeFile = enabled;
        this.logFilePath = filePath;
        this.fileLevel = level;
        return this;
    }

    public LoggerConfig withFile(boolean enabled, String filePath, Level level, boolean append, boolean immediateFlush) {
        this.includeFile = enabled;
        this.logFilePath = filePath;
        this.fileLevel = level;
        this.appendToFile = append;
        this.immediateFlush = immediateFlush;
        return this;
    }

    public LoggerConfig withAsyncBufferSize(int size) {
        this.asyncBufferSize = size;
        return this;
    }

    public LoggerConfig withAsyncMode(boolean mode) {
        this.asyncMode = mode;
        return this;
    }

    /**
     * Build and configure the logger based on this configuration.
     */
    public Logger build() throws IOException {
        // Add configured sinks
        if (includeConsole) {
            sinks.add(new StdOutSink(consoleLevel));
        }

        if (includeFile) {
            sinks.add(new FileSink(fileLevel, logFilePath, appendToFile, immediateFlush));
        }

        return new Logger(loggerName, dateFormat, messageFormat,
                asyncMode,
                asyncBufferSize,
                sinks.toArray(new LogMessageSink[0]));
    }
} 