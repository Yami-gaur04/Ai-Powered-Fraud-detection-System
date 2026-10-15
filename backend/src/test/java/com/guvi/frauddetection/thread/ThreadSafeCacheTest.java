package com.guvi.frauddetection.thread;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Multithreading - ThreadSafeCache Concurrency Test")
class ThreadSafeCacheTest {

    @Test
    @DisplayName("ComputeIfAbsent executes computation exactly once per key despite concurrent readers")
    void testComputeIfAbsentAtomic() throws InterruptedException {
        ThreadSafeCache<String, String> cache = new ThreadSafeCache<>();
        AtomicInteger computeCount = new AtomicInteger(0);
        int threads = 8;

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    cache.getOrCompute("KEY_1", k -> {
                        computeCount.incrementAndGet();
                        return "COMPUTED_VALUE";
                    });
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(1, computeCount.get(), "Mapping function must only be called once");
        assertEquals(Optional.of("COMPUTED_VALUE"), cache.get("KEY_1"));
    }

    @Test
    @DisplayName("Put and invalidate operations work properly")
    void testPutAndInvalidate() {
        ThreadSafeCache<String, Integer> cache = new ThreadSafeCache<>();
        cache.put("A", 100);
        cache.put("B", 200);

        assertEquals(2, cache.size());
        assertTrue(cache.containsKey("A"));

        Optional<Integer> removed = cache.invalidate("A");
        assertTrue(removed.isPresent());
        assertEquals(100, removed.get());
        assertFalse(cache.containsKey("A"));
        assertEquals(1, cache.size());

        cache.clear();
        assertEquals(0, cache.size());
    }
}
