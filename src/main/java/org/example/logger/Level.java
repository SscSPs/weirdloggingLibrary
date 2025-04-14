package org.example.logger;

public enum Level {
    DEBUG(0),
    INFO(1),
    WARN(2),
    ERROR(3),
    FATAL(4),
    ;
    private final int intLevel;

    Level(int intLevel) {
        this.intLevel = intLevel;
    }

    public int getLevelInt() {
        return intLevel;
    }
}
