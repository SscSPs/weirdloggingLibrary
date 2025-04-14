package org.example.logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Mapped Diagnostic Context for storing thread-local contextual data for logging.
 */
public class MDC {
    private static final ThreadLocal<Map<String, String>> contextMap =
            ThreadLocal.withInitial(HashMap::new);

    /**
     * Store a value in the current thread's context map.
     */
    public static void put(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        contextMap.get().put(key, value);
    }

    /**
     * Retrieve a value from the current thread's context map.
     */
    public static String get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        return contextMap.get().get(key);
    }

    /**
     * Remove a value from the current thread's context map.
     */
    public static void remove(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        contextMap.get().remove(key);
    }

    /**
     * Clear all entries in the current thread's context map.
     */
    public static void clear() {
        contextMap.get().clear();
    }

    /**
     * Get an immutable copy of the current thread's context map.
     */
    public static Map<String, String> getCopyOfContextMap() {
        return Collections.unmodifiableMap(new HashMap<>(contextMap.get()));
    }

    /**
     * Set the current thread's context map by copying values from the provided map.
     */
    public static void setContextMap(Map<String, String> contextMap) {
        if (contextMap == null) {
            MDC.contextMap.remove();
        } else {
            MDC.contextMap.set(new HashMap<>(contextMap));
        }
    }
} 