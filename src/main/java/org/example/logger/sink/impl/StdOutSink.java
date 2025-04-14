package org.example.logger.sink.impl;

import org.example.logger.Level;
import org.example.logger.sink.LogMessageSink;

public class StdOutSink extends LogMessageSink {
    public StdOutSink(Level sinkLevel) {
        super(sinkLevel);
    }

    private void writeToSelf(Level messageLevel, String message) {
        if (ignoreMessageAtLevel(messageLevel)) {
            return;
        }
        System.out.println("StdOutSink - " + message);
    }

    @Override
    public void consumeMessage(Level level, String oneMessage) {
        writeToSelf(level, oneMessage);
    }

    @Override
    public void flush() {
        //nothing to do for sync cases since everything is printed in sync
    }

}
