// © 2016-2017 Resurface Labs LLC

package io.resurface.tests;

import io.resurface.BaseLogger;
import io.resurface.JsonMessage;
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
    public void createsMultipleInstancesTest() {
        String agent1 = "agent1";
        String agent2 = "AGENT2";
        String agent3 = "aGeNt3";
        String url1 = "http://resurface.io";
        String url2 = "http://whatever.com";
        BaseLogger logger1 = new BaseLogger(agent1, url1);
        BaseLogger logger2 = new BaseLogger(agent2, url2);
        BaseLogger logger3 = new BaseLogger(agent3, "DEMO");

        expect(logger1.getAgent()).toEqual(agent1);
        expect(logger1.isEnabled()).toBeTrue();
        expect(logger1.getUrl()).toEqual(url1);
        expect(logger2.getAgent()).toEqual(agent2);
        expect(logger2.isEnabled()).toBeTrue();
        expect(logger2.getUrl()).toEqual(url2);
        expect(logger3.getAgent()).toEqual(agent3);
        expect(logger3.isEnabled()).toBeTrue();
        expect(logger3.getUrl()).toEqual(UsageLoggers.urlForDemo());

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
    public void hasValidVersionTest() {
        String version = BaseLogger.version_lookup();
        expect(version).toBeNotNull();
        expect(version.length()).toBeGreaterThan(0);
        expect(version).toStartWith("1.6.");
        expect(version.contains("\\")).toBeFalse();
        expect(version.contains("\"")).toBeFalse();
        expect(version.contains("'")).toBeFalse();
        expect(version).toEqual(new BaseLogger(MOCK_AGENT).getVersion());
    }

    @Test
    public void performsEnablingWhenExpectedTest() {
        BaseLogger logger = new BaseLogger(MOCK_AGENT, "DEMO", false);
        expect(logger.isEnabled()).toBeFalse();
        expect(logger.getUrl()).toEqual(UsageLoggers.urlForDemo());
        logger.enable();
        expect(logger.isEnabled()).toBeTrue();

        logger = new BaseLogger(MOCK_AGENT, UsageLoggers.urlForDemo(), true);
        expect(logger.isEnabled()).toBeTrue();
        expect(logger.getUrl()).toEqual(UsageLoggers.urlForDemo());
        logger.enable().disable().enable().disable().disable().disable().enable();
        expect(logger.isEnabled()).toBeTrue();

        List<String> queue = new ArrayList<>();
        logger = new BaseLogger(MOCK_AGENT, queue, false);
        expect(logger.isEnabled()).toBeFalse();
        expect(logger.getUrl()).toBeNull();
        logger.enable().disable().enable();
        expect(logger.isEnabled()).toBeTrue();
    }

    @Test
    public void skipsEnablingForInvalidUrlsTest() {
        for (String url : URLS_INVALID) {
            BaseLogger logger = new BaseLogger(MOCK_AGENT, url);
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
        expect(logger.isEnabled()).toBeFalse();
        expect(logger.getUrl()).toBeNull();
        logger.enable();
        expect(logger.isEnabled()).toBeFalse();
    }

    @Test
    public void skipsLoggingWhenDisabledTest() {
        for (String url : URLS_DENIED) {
            BaseLogger logger = new BaseLogger(MOCK_AGENT, url).disable();
            expect(logger.isEnabled()).toBeFalse();
            expect(logger.submit(null)).toBeTrue();  // would fail if enabled
        }
    }

    @Test
    public void submitsToDemoUrlTest() {
        BaseLogger logger = new BaseLogger(MOCK_AGENT, UsageLoggers.urlForDemo());
        expect(logger.getUrl()).toEqual(UsageLoggers.urlForDemo());
        StringBuilder json = new StringBuilder(64);
        JsonMessage.start(json, "test-https", logger.getAgent(), logger.getVersion(), System.currentTimeMillis());
        JsonMessage.stop(json);
        expect(logger.submit(json.toString())).toBeTrue();
    }

    @Test
    public void submitsToDemoUrlViaHttpTest() {
        BaseLogger logger = new BaseLogger(MOCK_AGENT, UsageLoggers.urlForDemo().replace("https://", "http://"));
        expect(logger.getUrl()).toStartWith("http://");
        StringBuilder json = new StringBuilder(64);
        JsonMessage.start(json, "test-http", logger.getAgent(), logger.getVersion(), System.currentTimeMillis());
        JsonMessage.stop(json);
        expect(logger.submit(json.toString())).toBeTrue();
    }

    @Test
    public void submitsToDeniedUrlAndFailsTest() {
        for (String url : URLS_DENIED) {
            BaseLogger logger = new BaseLogger(MOCK_AGENT, url);
            expect(logger.isEnabled()).toBeTrue();
            expect(logger.submit("{}")).toBeFalse();
        }
    }

    @Test
    public void submitsToQueueTest() {
        List<String> queue = new ArrayList<>();
        BaseLogger logger = new BaseLogger(MOCK_AGENT, queue);
        expect(logger.getUrl()).toBeNull();
        expect(logger.isEnabled()).toBeTrue();
        expect(queue.size()).toEqual(0);
        expect(logger.submit("{}")).toBeTrue();
        expect(queue.size()).toEqual(1);
        expect(logger.submit("{}")).toBeTrue();
        expect(queue.size()).toEqual(2);
    }

}
