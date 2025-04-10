package org.example.logger.sink.async;

import org.example.logger.Level;
import org.example.logger.sink.LogMessageSink;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SampleAsyncStdoutSink extends LogMessageSink {
    private final Queue<String> messagesQueue;
    private final int bufferSize;

    public SampleAsyncStdoutSink(Level sinkLevel, int bufferSize) {
        super(sinkLevel);
        this.messagesQueue = new ConcurrentLinkedQueue<>();
        this.bufferSize = bufferSize;
    }

    private void addToBuffer(Level messageLevel, String message) {
        if (ignoreMessageAtLevel(messageLevel)) {
            return;
        }
        synchronized (this) {
            if (messagesQueue.size() >= bufferSize) {
                this.flushBuffer();
//                flushingThread.start(); //this adds a new thread to flush the queue
            }
            messagesQueue.add(message);
        }
    }

    private void flushBuffer() {
        while (!messagesQueue.isEmpty()) {
            String message = messagesQueue.poll();
            if (message != null) {
                System.out.println("SampleAsyncStdoutSink - " + message);
            }
        }
    }

    @Override
    public void consumeMessage(Level level, String oneMessage) {
        addToBuffer(level, oneMessage);
    }

    @Override
    public void flush() {
        //if this is called, we want to flush the buffer for now.
        flushBuffer();
    }
}
