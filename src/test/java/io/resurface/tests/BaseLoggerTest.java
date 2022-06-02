// © 2016-2022 Resurface Labs Inc.

package io.resurface.tests;

import io.resurface.BaseLogger;
import io.resurface.Dispatcher;
import io.resurface.UsageLoggers;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static io.resurface.tests.Helper.*;
import static org.junit.Assert.fail;

/**
 * Tests against basic usage logger to embed or extend.
 */
public class BaseLoggerTest {

    @Test
    public void createsInstanceTest() {
        BaseLogger logger = new BaseLogger(MOCK_AGENT);
        expect(logger).toBeNotNull();
        expect(logger.getAgent()).toEqual(MOCK_AGENT);
        expect(logger.isEnableable()).toBeFalse();
        expect(logger.isEnabled()).toBeFalse();
        expect(logger.getQueue()).toBeNull();
        expect(logger.getUrl()).toBeNull();
        expect(logger.getMessageQueue()).toBeNull();
    }

    @Test
    public void createsMultipleInstancesTest() {
        String agent1 = "agent1";
        String agent2 = "AGENT2";
        String agent3 = "aGeNt3";
        String url1 = "http://resurface.io";
        String url2 = "http://whatever.com";
        BaseLogger logger1 = new BaseLogger(agent1, url1);
        BaseLogger logger2 = new BaseLogger(agent2, url2);
        BaseLogger logger3 = new BaseLogger(agent3, Helper.DEMO_URL);

        expect(logger1.getAgent()).toEqual(agent1);
        expect(logger1.isEnableable()).toBeTrue();
        expect(logger1.isEnabled()).toBeTrue();
        expect(logger1.getUrl()).toEqual(url1);
        expect(logger2.getAgent()).toEqual(agent2);
        expect(logger2.isEnableable()).toBeTrue();
        expect(logger2.isEnabled()).toBeTrue();
        expect(logger2.getUrl()).toEqual(url2);
        expect(logger3.getAgent()).toEqual(agent3);
        expect(logger3.isEnableable()).toBeTrue();
        expect(logger3.isEnabled()).toBeTrue();
        expect(logger3.getUrl()).toEqual(Helper.DEMO_URL);

        UsageLoggers.disable();
        expect(UsageLoggers.isEnabled()).toBeFalse();
        expect(logger1.isEnabled()).toBeFalse();
        expect(logger2.isEnabled()).toBeFalse();
        expect(logger3.isEnabled()).toBeFalse();
        UsageLoggers.enable();
        expect(UsageLoggers.isEnabled()).toBeTrue();
        expect(logger1.isEnabled()).toBeTrue();
        expect(logger2.isEnabled()).toBeTrue();
        expect(logger3.isEnabled()).toBeTrue();
    }

    @Test
    public void hasValidHostTest() {
        String host = BaseLogger.host_lookup();
        expect(host).toBeNotNull();
        expect(host.length()).toBeGreaterThan(0);
        expect(host.contentEquals("unknown")).toBeFalse();
        expect(host).toEqual(new BaseLogger(MOCK_AGENT).getHost());
    }

    @Test
    public void hasValidVersionTest() {
        String version = BaseLogger.version_lookup();
        expect(version).toBeNotNull();
        expect(version.length()).toBeGreaterThan(0);
        expect(version).toStartWith("2.2.");
        expect(version.contains("\\")).toBeFalse();
        expect(version.contains("\"")).toBeFalse();
        expect(version.contains("'")).toBeFalse();
        expect(version).toEqual(new BaseLogger(MOCK_AGENT).getVersion());
    }

    @Test
    public void performsEnablingWhenExpectedTest() {
        BaseLogger logger = new BaseLogger(MOCK_AGENT, Helper.DEMO_URL, false);
        expect(logger.isEnableable()).toBeTrue();
        expect(logger.isEnabled()).toBeFalse();
        expect(logger.getUrl()).toEqual(Helper.DEMO_URL);
        logger.enable();
        expect(logger.isEnabled()).toBeTrue();

        List<String> queue = new ArrayList<>();
        logger = new BaseLogger(MOCK_AGENT, queue, false);
        expect(logger.isEnableable()).toBeTrue();
        expect(logger.isEnabled()).toBeFalse();
        expect(logger.getUrl()).toBeNull();
        logger.enable().disable().enable();
        expect(logger.isEnabled()).toBeTrue();
    }

    @Test
    public void skipsEnablingForInvalidUrlsTest() {
        for (String url : MOCK_URLS_INVALID) {
            BaseLogger logger = new BaseLogger(MOCK_AGENT, url);
            expect(logger.isEnableable()).toBeFalse();
            expect(logger.isEnabled()).toBeFalse();
            expect(logger.getUrl()).toBeNull();
            logger.enable();
            expect(logger.isEnabled()).toBeFalse();
        }
    }

    @Test
    public void skipsEnablingForNullUrlTest() {
        String url = null;
        BaseLogger logger = new BaseLogger(MOCK_AGENT, url);
        expect(logger.isEnableable()).toBeFalse();
        expect(logger.isEnabled()).toBeFalse();
        expect(logger.getUrl()).toBeNull();
        logger.enable();
        expect(logger.isEnabled()).toBeFalse();
    }

    @Test
    public void submitsToDeniedUrlTest() throws InterruptedException {
        for (String url : MOCK_URLS_DENIED) {
            BaseLogger logger = new BaseLogger(MOCK_AGENT, url);
            expect(logger.isEnableable()).toBeTrue();
            expect(logger.isEnabled()).toBeTrue();
            logger.submit("{}");
            logger.stop_dispatcher();
            expect(logger.getSubmitFailures()).toEqual(1);
            expect(logger.getSubmitSuccesses()).toEqual(0);
        }
    }

    @Test
    public void submitsToQueueTest() throws InterruptedException {
        List<String> queue = new ArrayList<>();
        BaseLogger logger = new BaseLogger(MOCK_AGENT, queue);
        logger.init_dispatcher();
        expect(logger.getQueue()).toEqual(queue);
        expect(logger.getUrl()).toBeNull();
        expect(logger.isEnableable()).toBeTrue();
        expect(logger.isEnabled()).toBeTrue();
        expect(queue.size()).toEqual(0);
        logger.submit("{}");
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(1);
        logger.init_dispatcher();
        logger.submit("{}");
        logger.stop_dispatcher();
        expect(queue.size()).toEqual(2);
        expect(logger.getSubmitFailures()).toEqual(0);
        expect(logger.getSubmitSuccesses()).toEqual(2);
    }

    @Test
    public void usesSkipOptionsTest() {
        BaseLogger logger = new BaseLogger(MOCK_AGENT, Helper.DEMO_URL);
        expect(logger.getSkipCompression()).toBeFalse();
        expect(logger.getSkipSubmission()).toBeFalse();

        logger.setSkipCompression(true);
        expect(logger.getSkipCompression()).toBeTrue();
        expect(logger.getSkipSubmission()).toBeFalse();

        logger.setSkipCompression(false);
        logger.setSkipSubmission(true);
        expect(logger.getSkipCompression()).toBeFalse();
        expect(logger.getSkipSubmission()).toBeTrue();
    }

    @Test
    public void messageQueueFillTest() {
        List<String> queue = new ArrayList<>();
        BaseLogger logger = new BaseLogger(MOCK_AGENT, queue);
        for (int i = 0; i < 128; i++) {
            logger.submit(MOCK_MESSAGE);
        }
        expect(logger.getMessageQueue().isEmpty()).toBeFalse();
        expect(logger.getMessageQueue().size()).toEqual(128);
        expect(logger.getMessageQueue().remainingCapacity()).toEqual(0);
    }

    @Test
    public void messageQueueBlockingTakeTest() {
        List<String> queue = new ArrayList<>();
        BaseLogger logger = new BaseLogger(MOCK_AGENT, queue);
        Thread taker = new Thread(() -> {
            try {
                Object unused = logger.getMessageQueue().take();
                fail();
            } catch (InterruptedException success) {
                // success!
            }
        });
        try {
            expect(logger.getMessageQueue().isEmpty()).toBeTrue();
            taker.start();
            Thread.sleep(1000);
            expect(logger.getMessageQueue().isEmpty()).toBeTrue();
            taker.interrupt();
            taker.join(1000);
            expect(taker.isAlive()).toBeFalse();
        } catch (Exception unexpected) {
            unexpected.printStackTrace();
        }
    }

    @Test
    public void messageQueueBlockingPutTest() {
        List<String> queue = new ArrayList<>();
        BaseLogger logger = new BaseLogger(MOCK_AGENT, queue);
        Thread taker = new Thread(() -> {
            try {
                logger.getMessageQueue().put(MOCK_MESSAGE);
                fail();
            } catch (InterruptedException success) {
                // success!
            }
        });
        try {
            for (int i = 0; i < 128; i++) {
                logger.submit(MOCK_MESSAGE);
            }
            expect(logger.getMessageQueue().remainingCapacity()).toEqual(0);
            taker.start();
            Thread.sleep(1000);
            expect(logger.getMessageQueue().remainingCapacity()).toEqual(0);
            taker.interrupt();
            taker.join(1000);
            expect(taker.isAlive()).toBeFalse();
        } catch (Exception unexpected) {
            unexpected.printStackTrace();
        }
    }

    @Test
    public void messageQueueNoBlockingTakeTest() {
        List<String> queue = new ArrayList<>();
        BaseLogger logger = new BaseLogger(MOCK_AGENT, queue);
        Thread taker = new Thread(() -> {
            try {
                Object unused = logger.getMessageQueue().take();
            } catch (InterruptedException failure) {
                fail();
            }
        });
        try {
            taker.start();
            Thread.sleep(1000);
            logger.submit(MOCK_MESSAGE);
            taker.join(1000);
            expect(taker.isAlive()).toBeFalse();
        } catch (Exception unexpected) {
            unexpected.printStackTrace();
        }
    }

    @Test
    public void messageQueueNoBlockingPutTest() {
        List<String> queue = new ArrayList<>();
        BaseLogger logger = new BaseLogger(MOCK_AGENT, queue);
        Thread taker = new Thread(() -> {
            try {
                logger.getMessageQueue().put(MOCK_MESSAGE);
            } catch (InterruptedException failure) {
                fail();
            }
        });
        try {
            for (int i = 0; i < 100; i++) {
                logger.submit(MOCK_MESSAGE);
            }
            taker.start();
            Thread.sleep(1000);
            logger.submit(MOCK_MESSAGE);
            taker.join(1000);
            expect(taker.isAlive()).toBeFalse();
        } catch (Exception unexpected) {
            unexpected.printStackTrace();
        }
    }

    @Test
    public void messageQueuePutTakeTest() {
        final AtomicInteger putSum = new AtomicInteger(0);
        final AtomicInteger takeSum = new AtomicInteger(0);
        List<String> queue = new ArrayList<>();
        BaseLogger logger = new BaseLogger(MOCK_AGENT, queue);
        Thread producer = new Thread(() -> {
            try {
                int seed = (this.hashCode() ^ (int)System.nanoTime());
                int sum = 0;
                for (int i = 0; i < 128; i++) {
                    logger.getMessageQueue().put(seed);
                    sum += seed;
                    seed ^= (seed << 3);
                    seed ^= (seed >>> 13);
                    seed ^= (seed << 11);
                }
                putSum.getAndAdd(sum);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Thread consumer = new Thread(() -> {
            try {
                int sum = 0;
                for (int i = 0; i < 128; i++) {
                    sum += (int) logger.getMessageQueue().take();
                }
                takeSum.getAndAdd(sum);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        try {
            producer.start();
            consumer.start();
            Thread.sleep(100);
            producer.join(1000);
            expect(producer.isAlive()).toBeFalse();
            consumer.join(1000);
            expect(consumer.isAlive()).toBeFalse();
            expect(putSum.get()).toEqual(takeSum.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void setMessageQueueTest() {
        BaseLogger logger = new BaseLogger(MOCK_AGENT);
        logger.setMessageQueue();
        expect(logger.getMessageQueue().isEmpty()).toBeTrue();
        expect(logger.getMessageQueue().size()).toEqual(0);
        expect(logger.getMessageQueue().remainingCapacity()).toEqual(128);
    }

    @Test
    public void dispatcherTest() throws InterruptedException {
        BaseLogger logger = new BaseLogger(MOCK_AGENT);

        logger.init_dispatcher();
        expect(logger.getMessageQueue().isEmpty()).toBeTrue();
        expect(logger.getMessageQueue().size()).toEqual(0);
        expect(logger.getMessageQueue().remainingCapacity()).toEqual(128);
        expect(logger.isWorkerAlive()).toBeTrue();

        logger.submit(MOCK_MESSAGE);
        logger.stop_dispatcher();
        expect(logger.isWorkerAlive()).toBeFalse();

    }

    @Test
    public void singleBatchTest() throws InterruptedException {
        final int MESSAGE_WNL_LENGTH = (MOCK_MESSAGE + "\n").length();
        StringBuilder Messages = new StringBuilder(MESSAGE_WNL_LENGTH);
        for (int i = 0; i < 10; i++) {
            Messages.append(MOCK_MESSAGE + "\n");
        }
        final String NDJSON_BATCH = Messages.toString();

        List<String> queue = new ArrayList<>();
        BaseLogger logger = new BaseLogger(MOCK_AGENT, queue);

        logger.init_dispatcher();
        for (int i = 0; i < 10; i++) {
            logger.submit(MOCK_MESSAGE);
        }
        logger.stop_dispatcher();

        expect(queue.size()).toEqual(1);
        expect(queue.get(0)).toEqual(NDJSON_BATCH);
        expect(queue.get(0).length()).toEqual(MESSAGE_WNL_LENGTH * 10);
    }

    @Test
    public void multiBatchTest() throws InterruptedException {
        StringBuilder Messages;
        final int N_BATCHES = 6;
        final int[] BATCH_SIZES = { 0, 1, 10, 20, 400, 420, 421, 10 * 1024, 50 * 1024 };
        for (int batchSize: BATCH_SIZES) {
            final int MESSAGE_COUNT = batchSize <= (MOCK_MESSAGE).length() ? 1 : batchSize / (MOCK_MESSAGE).length();

            Messages = new StringBuilder(MESSAGE_COUNT);
            for (int i = 0; i < MESSAGE_COUNT * N_BATCHES; i++) {
                Messages.append(MOCK_MESSAGE + "\n");
            }
            final String NDJSON_MESSAGE = Messages.toString();

            List<String> queue = new ArrayList<>();
            BaseLogger logger = new BaseLogger(MOCK_AGENT, queue);

            logger.init_dispatcher(batchSize);
            for (int i = 0; i < MESSAGE_COUNT * N_BATCHES; i++) {
                logger.submit(MOCK_MESSAGE);
            }
            logger.stop_dispatcher();

            expect(queue.size()).toBeGreaterThan(N_BATCHES - 1);

            Messages = new StringBuilder(MESSAGE_COUNT);
            for (String s : queue) {
                Messages.append(s);
            }
            expect(Messages.toString()).toEqual(NDJSON_MESSAGE);
        }
    }
}
