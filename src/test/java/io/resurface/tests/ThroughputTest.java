package io.resurface.tests;

import io.resurface.BaseLogger;
import io.resurface.Dispatcher;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static io.resurface.tests.Helper.MOCK_AGENT;


public class ThroughputTest {
    @Test
    public void timedArrayMessageQueueTest() {
        for (int i = 1; i < 10000; i*=10) {
            System.out.printf("ARRAY BLOCKING QUEUE (message count = %d)%n", i);
            List<String> queue = new ArrayList<>();
            BaseLogger logger = new BaseLogger(MOCK_AGENT, queue, true, 100);
            timedMessageQueuePutTakeTest(logger, i, 1000);
        }
        System.out.println();
    }

    @Test
    public void timedLinkedMessageQueueTest() {
        for (int i = 1; i < 10000; i*=10) {
            System.out.printf("LINKED BLOCKING QUEUE (message count = %d)%n", i);
            List<String> queue = new ArrayList<>();
            BaseLogger logger = new LinkedBaseLogger(queue, 100);
            timedMessageQueuePutTakeTest(logger, i, 1000);
        }
        System.out.println();
    }

    private void timedMessageQueuePutTakeTest(BaseLogger logger, int messageCount, int iterations) {
        final long[] results = new long[iterations];

        for (int i = 0; i < iterations; i++) {
            final BarrierTimer timer = new BarrierTimer();
            final CyclicBarrier barrier = new CyclicBarrier(2, timer);
            final AtomicInteger putSum = new AtomicInteger(0);
            final AtomicInteger takeSum = new AtomicInteger(0);

            Thread producer = new Thread(() -> {
                try {
                    int seed = (this.hashCode() ^ (int)System.nanoTime());
                    int sum = 0;
                    for (int j = 0; j < messageCount; j++) {
                        logger.getMessageQueue().put(seed);
                        sum += seed;
                        seed ^= (seed << 3);
                        seed ^= (seed >>> 13);
                        seed ^= (seed << 11);
                    }
                    putSum.getAndAdd(sum);
                    barrier.await();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Thread consumer = new Thread(() -> {
                try {
                    int sum = 0;
                    for (int j = 0; j < messageCount; j++) {
                        sum += (int) logger.getMessageQueue().take();
                    }
                    takeSum.getAndAdd(sum);
                    barrier.await();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            try {
                producer.start();
                consumer.start();
                barrier.await();
                barrier.await();
                results[i] = timer.getTime() / (2L * messageCount);
                producer.join();
                consumer.join();
                expect(producer.isAlive()).toBeFalse();
                expect(consumer.isAlive()).toBeFalse();
                expect(putSum.get()).toEqual(takeSum.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        long nsPerItem = 0;
        for (long result: results) {
            nsPerItem += result;
        }
        nsPerItem /= iterations;

        long stDev = 0;
        for (long result: results) {
            stDev += (long) Math.pow((result- nsPerItem), 2);
        }
        stDev /= iterations;
        stDev = (long) Math.sqrt(stDev);

        System.out.printf("Throughput: %d ns/item (SD = %d)%n", nsPerItem, stDev);
    }

    private static class BarrierTimer implements Runnable {
        private boolean started;
        private long startTime, endTime;
        public synchronized void run() {
            long t = System.nanoTime();
            if (!started) {
                started = true;
                startTime = t;
            } else
                endTime = t;
        }
        public synchronized void clear() {
            started = false;
        }
        public synchronized long getTime() {
            return endTime - startTime;
        }
    }

    private static class LinkedBaseLogger extends BaseLogger<LinkedBaseLogger> {

        /**
         * Initialize enabled logger using default url.
         */
        public LinkedBaseLogger() {
            super(MOCK_AGENT);
        }

        /**
         * Initialize enabled logger using queue.
         */
        public LinkedBaseLogger(List<String> queue) {
            super(MOCK_AGENT, queue);
        }

        /**
         * Initialize enabled logger using queue and message queue bound.
         */
        public LinkedBaseLogger(List<String> queue, int max_queue_depth) {
            super(MOCK_AGENT, queue, true, max_queue_depth);
        }

        @Override
        public void setMessageQueue(int max_queue_depth) {
            this.msg_queue = new LinkedBlockingQueue<>(max_queue_depth);
        }
    }
}
