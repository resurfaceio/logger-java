// © 2016-2021 Resurface Labs Inc.

package io.resurface.tests;

import io.resurface.BaseLogger;
import io.resurface.Json;
import io.resurface.UsageLoggers;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static io.resurface.tests.Helper.*;

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
    public void submitsToDeniedUrlTest() {
        for (String url : MOCK_URLS_DENIED) {
            BaseLogger logger = new BaseLogger(MOCK_AGENT, url);
            expect(logger.isEnableable()).toBeTrue();
            expect(logger.isEnabled()).toBeTrue();
            logger.submit("{}");
            expect(logger.getSubmitFailures()).toEqual(1);
            expect(logger.getSubmitSuccesses()).toEqual(0);
        }
    }

    @Test
    public void submitsToQueueTest() {
        List<String> queue = new ArrayList<>();
        BaseLogger logger = new BaseLogger(MOCK_AGENT, queue);
        expect(logger.getQueue()).toEqual(queue);
        expect(logger.getUrl()).toBeNull();
        expect(logger.isEnableable()).toBeTrue();
        expect(logger.isEnabled()).toBeTrue();
        expect(queue.size()).toEqual(0);
        logger.submit("{}");
        expect(queue.size()).toEqual(1);
        logger.submit("{}");
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

}
