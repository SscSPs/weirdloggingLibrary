package org.example;

import org.example.logger.Level;
import org.example.logger.Logger;
import org.example.logger.sink.LogMessageSink;
import org.example.logger.sink.async.SampleAsyncStdoutSink;
import org.example.logger.sink.sync.StdOutSink;

public class Main {
    public static void main(String[] args) {
        LogMessageSink syncMessageSink = new StdOutSink(Level.DEBUG);
        LogMessageSink asyncMessageSink = new SampleAsyncStdoutSink(Level.INFO, 2);

        Logger logger = new Logger( "LoggerName",
                "yyyy-MM-dd HH:mm:ss",
                "{TIMESTAMP} [{LEVEL}] - {MESSAGE}",
                syncMessageSink, asyncMessageSink);

        for (int i  = 0; i<10; i++){
            final String  threadName = "Thread"+i;
            Main.sendMessagedToLogger(logger, threadName + " message ", Level.DEBUG, 1);
            Main.sendMessagedToLogger(logger, threadName + " message ", Level.INFO, 1);
        }
    }

    private static void sendMessagedToLogger(Logger logger, String message, Level level, int count){
        while (count-- > 0){
            logger.log(level, count + " - " + message);
        }
    }

}
