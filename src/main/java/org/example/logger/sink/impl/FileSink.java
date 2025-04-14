package org.example.logger.sink.impl;

import org.example.logger.Level;
import org.example.logger.sink.LogMessageSink;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A synchronous file-based logging sink implementation
 */
public class FileSink extends LogMessageSink {
    private final String logFilePath;
    private final PrintWriter writer;
    private final boolean autoFlush;

    /**
     * Creates a new file sink with the specified log level and file path
     *
     * @param sinkLevel   minimum level to log
     * @param logFilePath path to the log file
     * @param append      whether to append to existing file or overwrite
     * @param autoFlush   whether to automatically flush after each write
     * @throws IOException if there's an error creating or opening the log file
     */
    public FileSink(Level sinkLevel, String logFilePath, boolean append, boolean autoFlush) throws IOException {
        super(sinkLevel);
        this.logFilePath = logFilePath;
        this.autoFlush = autoFlush;

        // Create directory if it doesn't exist
        Path path = Paths.get(logFilePath);
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        // Initialize writer
        FileWriter fileWriter = new FileWriter(logFilePath, append);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        this.writer = new PrintWriter(bufferedWriter, autoFlush);
    }

    /**
     * Creates a new file sink with the specified log level and file path,
     * with default settings (append=true, autoFlush=true)
     *
     * @param sinkLevel   minimum level to log
     * @param logFilePath path to the log file
     * @throws IOException if there's an error creating or opening the log file
     */
    public FileSink(Level sinkLevel, String logFilePath) throws IOException {
        this(sinkLevel, logFilePath, true, true);
    }

    private void writeToFile(Level messageLevel, String message) {
        if (ignoreMessageAtLevel(messageLevel)) {
            return;
        }

        writer.println(message);

        if (!autoFlush) {
            // Only check for errors if not auto-flushing
            if (writer.checkError()) {
                System.err.println("Error writing to log file: " + logFilePath);
            }
        }
    }

    @Override
    public void consumeMessage(Level level, String oneMessage) {
        writeToFile(level, oneMessage);
    }

    @Override
    public void flush() {
        writer.flush();
    }

    /**
     * Closes the log file. This method should be called when the sink is no longer needed.
     */
    public void close() {
        flush();
        writer.close();
    }
} 