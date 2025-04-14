package org.example.logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MDCTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldStoreAndRetrieveValues() {
        // When
        MDC.put("key1", "value1");
        MDC.put("key2", "value2");

        // Then
        assertEquals("value1", MDC.get("key1"));
        assertEquals("value2", MDC.get("key2"));
        assertNull(MDC.get("nonexistent"));
    }

    @Test
    void shouldRemoveSpecificValue() {
        // Given
        MDC.put("key1", "value1");
        MDC.put("key2", "value2");

        // When
        MDC.remove("key1");

        // Then
        assertNull(MDC.get("key1"));
        assertEquals("value2", MDC.get("key2"));
    }

    @Test
    void shouldClearAllValues() {
        // Given
        MDC.put("key1", "value1");
        MDC.put("key2", "value2");

        // When
        MDC.clear();

        // Then
        assertNull(MDC.get("key1"));
        assertNull(MDC.get("key2"));
    }

    @Test
    void shouldRejectNullKey() {
        assertThrows(IllegalArgumentException.class, () -> MDC.put(null, "value"));
        assertThrows(IllegalArgumentException.class, () -> MDC.get(null));
        assertThrows(IllegalArgumentException.class, () -> MDC.remove(null));
    }

    @Test
    void shouldCopyContextMap() {
        // Given
        MDC.put("key1", "value1");
        MDC.put("key2", "value2");

        // When
        Map<String, String> copy = MDC.getCopyOfContextMap();

        // Then
        assertEquals(2, copy.size());
        assertEquals("value1", copy.get("key1"));
        assertEquals("value2", copy.get("key2"));

        // Verify the copy is immutable
        assertThrows(UnsupportedOperationException.class, () -> copy.put("key3", "value3"));
    }

    @Test
    void shouldSetContextMap() {
        // Given
        Map<String, String> newMap = new HashMap<>();
        newMap.put("key1", "value1");
        newMap.put("key2", "value2");

        // When
        MDC.setContextMap(newMap);

        // Then
        assertEquals("value1", MDC.get("key1"));
        assertEquals("value2", MDC.get("key2"));

        // When - modify original map
        newMap.put("key3", "value3");

        // Then - MDC should not be affected
        assertNull(MDC.get("key3"));
    }

    @Test
    void shouldHandleNullContextMap() {
        // Given
        MDC.put("key1", "value1");

        // When
        MDC.setContextMap(null);

        // Then
        assertNull(MDC.get("key1"));
    }

    @Test
    void shouldMaintainThreadLocalStorage() throws Exception {
        // Given
        MDC.put("main", "mainValue");
        CountDownLatch threadStarted = new CountDownLatch(1);
        CountDownLatch threadCompleted = new CountDownLatch(1);

        // When - run in another thread
        Thread thread = new Thread(() -> {
            assertNull(MDC.get("main")); // Should not see main thread's value
            MDC.put("thread", "threadValue");
            threadStarted.countDown();

            try {
                Thread.sleep(100); // Hold the thread to ensure test assertions run
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            threadCompleted.countDown();
        });
        thread.start();

        // Then
        threadStarted.await(1, TimeUnit.SECONDS);
        assertEquals("mainValue", MDC.get("main")); // Main thread should still have its value
        assertNull(MDC.get("thread")); // Main thread should not see other thread's value

        threadCompleted.await(1, TimeUnit.SECONDS);
    }
} 