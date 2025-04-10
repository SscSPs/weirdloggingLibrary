package org.example.logger.sink;

import org.example.logger.Level;

public abstract class LogMessageSink {
    Level sinkLevel;

    protected LogMessageSink(Level sinkLevel){
        this.sinkLevel = sinkLevel;
    }
    protected boolean ignoreMessageAtLevel(Level messageLevel) {
        return messageLevel.getLevelInt() < sinkLevel.getLevelInt();
    }

    public abstract void consumeMessage(Level level, String oneMessage);

    public abstract void flush();
}
