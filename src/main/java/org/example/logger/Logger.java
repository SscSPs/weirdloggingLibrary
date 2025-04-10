package org.example.logger;

import org.example.logger.sink.LogMessageSink;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Logger {
    private final String loggerName;
    private final List<LogMessageSink> sinks = new ArrayList<>();
    private final DateTimeFormatter dateTimeFormat;
    //supported message keywords - TIMESTAMP, LEVEL, MESSAGE
    private final String messageFormat;

    // private final Level loggerLevel; //?? why do we have level here as well as in sink? maybe multi level log filtering?

    public Logger(String loggerName, String timeFormat, String messageFormat, LogMessageSink... sinks) {
        this.loggerName = loggerName;
        this.sinks.addAll(Arrays.asList(sinks));
        // this.loggerLevel = level;
        this.dateTimeFormat = DateTimeFormatter.ofPattern(timeFormat);
        this.messageFormat = messageFormat;

        //this will flush any remaining messages when application is shutting down
        Thread loggerFlush = new Thread(this::flush);
        Runtime.getRuntime().addShutdownHook(loggerFlush);
    }

    private String formatMessage(Level messageLevel, String message){
        LocalDateTime date = LocalDateTime.now();
        String formattedTime = date.format(dateTimeFormat);

        String formattedMessage = messageFormat.replace("{TIMESTAMP}", formattedTime);
        formattedMessage = formattedMessage.replace("{LEVEL}", messageLevel.name());
        return formattedMessage.replace("{MESSAGE}", message);
    }

    public void log(Level messageLevel, String message) {
        if (messageLevel == null || message == null || message.isEmpty()){
            //empty message or log level not defined,
            //not logging anyting;
            return;
        }
        String formattedMessage = formatMessage(messageLevel, message);
        sendMessageToEachSink(messageLevel, formattedMessage);
    }

    private void sendMessageToEachSink(Level messageLevel, String formattedMessage) {
        for(LogMessageSink oneSink : sinks){
            oneSink.consumeMessage(messageLevel, formattedMessage);
        }
    }

    public void flush() {
        sinks.forEach(LogMessageSink::flush);
    }
}
