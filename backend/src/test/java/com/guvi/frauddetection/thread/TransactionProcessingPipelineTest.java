package com.guvi.frauddetection.thread;

import com.guvi.frauddetection.entity.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Multithreading - TransactionProcessingPipeline Producer-Consumer Test")
class TransactionProcessingPipelineTest {

    private TransactionProcessingPipeline pipeline;
    private FraudMetricsCounter metricsCounter;

    @BeforeEach
    void setUp() {
        metricsCounter = new FraudMetricsCounter();
        pipeline = new TransactionProcessingPipeline(metricsCounter);
        pipeline.start();
    }

    @AfterEach
    void tearDown() {
        if (pipeline != null) {
            pipeline.stop();
        }
    }

    @Test
    @DisplayName("Producer can submit transactions and consumer workers process them concurrently")
    void testProducerConsumerFlow() throws InterruptedException {
        int transactionCount = 10;
        CountDownLatch consumedLatch = new CountDownLatch(transactionCount);
        AtomicInteger consumedCount = new AtomicInteger(0);

        pipeline.setTransactionConsumer(tx -> {
            consumedCount.incrementAndGet();
            consumedLatch.countDown();
        });

        for (int i = 0; i < transactionCount; i++) {
            Transaction tx = Transaction.builder()
                    .id((long) (i + 1))
                    .amount(BigDecimal.valueOf(100.00 + i))
                    .merchant("Merchant-" + i)
                    .location("City-" + i)
                    .deviceId("dev-" + i)
                    .paymentMethod("ONLINE")
                    .transactionTime(LocalDateTime.now())
                    .build();

            boolean enqueued = pipeline.submitTransaction(tx, 1000);
            assertTrue(enqueued, "Transaction should be enqueued successfully");
        }

        boolean allConsumed = consumedLatch.await(5, TimeUnit.SECONDS);
        assertTrue(allConsumed, "All transactions should be consumed by background workers");
        assertEquals(transactionCount, consumedCount.get());
    }
}
