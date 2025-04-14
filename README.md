# Weird Logging Library

A lightweight and flexible logging library for Java applications with support for Mapped Diagnostic Context (MDC) and multiple output sinks.

## Overview

Weird Logging Library is a Java logging solution that provides a simple yet powerful interface for application logging. It features a builder pattern for configuration, support for MDC (Mapped Diagnostic Context), and multiple output sinks including console and file logging.

## Features

- Multiple logging levels (DEBUG, INFO, WARN, ERROR, FATAL)
- Configurable log formats with timestamp and MDC support
- Multiple output sinks (console, file)
- Mapped Diagnostic Context (MDC) for contextual logging
- Thread-safe logging with MDC support
- Builder pattern for easy configuration
- Exception logging support
- Multi-threaded logging capabilities

## Installation

### Maven
```xml
<dependency>
    <groupId>org.example</groupId>
    <artifactId>weirdloggingLibrary</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### Gradle
```groovy
implementation 'org.example:weirdloggingLibrary:1.0-SNAPSHOT'
```

## Quick Start

```java
import org.example.logger.Logger;
import org.example.logger.LoggerConfig;
import org.example.logger.Level;

public class Main {
    public static void main(String[] args) throws IOException {
        // Create a logger with builder API
        Logger logger = new LoggerConfig()
                .withName("MainLogger")
                .withDateFormat("yyyy-MM-dd HH:mm:ss")
                .withMessageFormat("{TIMESTAMP} [{LEVEL}] [{LOGGER}] [{MDC:requestId}] - {MESSAGE}")
                .withConsole(true, Level.DEBUG)
                .withFile(true, "logs/application.log", Level.INFO)
                .build();

        // Basic logging
        logger.debug("Debug message");
        logger.info("Info message");
        logger.warn("Warning message");
        logger.error("Error message");
        logger.fatal("Fatal message");
    }
}
```

## Advanced Usage

### MDC (Mapped Diagnostic Context) Logging

```java
import org.example.logger.Logger;
import org.example.logger.MDC;

public class Main {
    public static void main(String[] args) {
        Logger logger = // ... initialize logger
        
        // Set MDC context
        MDC.put("requestId", "USER-123");
        logger.info("User authenticated");
        
        // Clear MDC when done
        MDC.clear();
    }
}
```

### Exception Logging

```java
try {
    // Some code that might throw an exception
    int result = 10 / 0;
} catch (Exception e) {
    MDC.put("requestId", "ERROR-999");
    logger.error("Calculation error", e);
    MDC.clear();
}
```

### Multi-threaded Logging

```java
new Thread(() -> {
    MDC.put("requestId", "THREAD-1");
    logger.info("Thread started");
    
    // ... thread work ...
    
    logger.info("Thread finished");
    MDC.clear();
}).start();
```

## Configuration Options

The `LoggerConfig` builder supports the following configuration options:

- `withName(String name)`: Set the logger name
- `withDateFormat(String format)`: Set the timestamp format
- `withMessageFormat(String format)`: Set the log message format
- `withConsole(boolean enabled, Level level)`: Configure console output
- `withFile(boolean enabled, String path, Level level)`: Configure file output

## Message Format

The log message format supports the following placeholders:
- `{TIMESTAMP}`: Current timestamp
- `{LEVEL}`: Log level
- `{LOGGER}`: Logger name
- `{MDC:key}`: MDC value for the specified key
- `{MESSAGE}`: Log message

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Authors

- Your Name - Initial work

## Acknowledgments

- Thanks to all contributors who have helped shape this project
