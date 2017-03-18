// © 2016-2017 Resurface Labs LLC

package io.resurface.tests;

import io.resurface.BaseLogger;
import io.resurface.JsonMessage;
import io.resurface.UsageLoggers;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static io.resurface.tests.Helper.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests against basic usage logger to embed or extend.
 */
public class BaseLoggerTest {

    @Test
    public void managesMultipleInstancesTest() {
        String agent1 = "agent1";
        String agent2 = "AGENT2";
        String agent3 = "aGeNt3";
        String url1 = "http://resurface.io";
        String url2 = "http://whatever.com";
        BaseLogger logger1 = new BaseLogger(agent1, url1);
        BaseLogger logger2 = new BaseLogger(agent2, url2);
        BaseLogger logger3 = new BaseLogger(agent3, "DEMO");

        assertEquals(logger1.getAgent(), agent1);
        assertEquals(logger1.isEnabled(), true);
        assertEquals(logger1.getUrl(), url1);
        assertEquals(logger2.getAgent(), agent2);
        assertEquals(logger2.isEnabled(), true);
        assertEquals(logger2.getUrl(), url2);
        assertEquals(logger3.getAgent(), agent3);
        assertEquals(logger3.isEnabled(), true);
        assertEquals(logger3.getUrl(), UsageLoggers.urlForDemo());

        UsageLoggers.disable();
        assertEquals(UsageLoggers.isEnabled(), false);
        assertEquals(logger1.isEnabled(), false);
        assertEquals(logger2.isEnabled(), false);
        assertEquals(logger3.isEnabled(), false);
        UsageLoggers.enable();
        assertEquals(UsageLoggers.isEnabled(), true);
        assertEquals(logger1.isEnabled(), true);
        assertEquals(logger2.isEnabled(), true);
        assertEquals(logger3.isEnabled(), true);
    }

    @Test
    public void providesValidVersionTest() {
        String version = BaseLogger.version_lookup();
        assertTrue("null check", version != null);
        assertTrue("length check", version.length() > 0);
        assertTrue("startsWith check", version.startsWith("1.6."));
        assertTrue("backslash check", !version.contains("\\"));
        assertTrue("double quote check", !version.contains("\""));
        assertTrue("single quote check", !version.contains("'"));
        assertEquals(version, new BaseLogger(MOCK_AGENT).getVersion());
    }

    @Test
    public void performsEnablingWhenExpectedTest() {
        BaseLogger logger = new BaseLogger(MOCK_AGENT, "DEMO", false);
        assertTrue("logger disabled", !logger.isEnabled());
        assertTrue("url matches", UsageLoggers.urlForDemo().equals(logger.getUrl()));
        logger.enable();
        assertTrue("logger enabled", logger.isEnabled());

        logger = new BaseLogger(MOCK_AGENT, UsageLoggers.urlForDemo(), true);
        assertTrue("logger enabled", logger.isEnabled());
        assertTrue("url matches", UsageLoggers.urlForDemo().equals(logger.getUrl()));
        logger.enable().disable().enable().disable().disable().disable().enable();
        assertTrue("logger enabled", logger.isEnabled());

        List<String> queue = new ArrayList<>();
        logger = new BaseLogger(MOCK_AGENT, queue, false);
        assertTrue("logger disabled", !logger.isEnabled());
        assertTrue("url is null", logger.getUrl() == null);
        logger.enable().disable().enable();
        assertTrue("logger enabled", logger.isEnabled());
    }

    @Test
    public void skipsEnablingForInvalidUrlsTest() {
        for (String url : URLS_INVALID) {
            BaseLogger logger = new BaseLogger(MOCK_AGENT, url);
            assertTrue("logger disabled at first", !logger.isEnabled());
            assertTrue("url is null", logger.getUrl() == null);
            logger.enable();
            assertTrue("logger still disabled", !logger.isEnabled());
        }
    }

    @Test
    public void skipsEnablingForNullUrlTest() {
        String url = null;
        BaseLogger logger = new BaseLogger(MOCK_AGENT, url);
        assertTrue("logger disabled at first", !logger.isEnabled());
        assertTrue("url is null", logger.getUrl() == null);
        logger.enable();
        assertTrue("logger still disabled", !logger.isEnabled());
    }

    @Test
    public void skipsLoggingWhenDisabledTest() {
        for (String url : URLS_DENIED) {
            BaseLogger logger = new BaseLogger(MOCK_AGENT, url).disable();
            assertTrue("logger disabled", !logger.isEnabled());
            assertTrue("log succeeds", logger.submit(null));  // would fail if enabled
        }
    }

    @Test
    public void submitsToDemoUrlTest() {
        BaseLogger logger = new BaseLogger(MOCK_AGENT, UsageLoggers.urlForDemo());
        assertTrue("url matches", UsageLoggers.urlForDemo().equals(logger.getUrl()));
        StringBuilder json = new StringBuilder(64);
        JsonMessage.start(json, "test-https", logger.getAgent(), logger.getVersion(), System.currentTimeMillis());
        JsonMessage.stop(json);
        assertTrue("submit succeeds", logger.submit(json.toString()));
    }

    @Test
    public void submitsToDemoUrlViaHttpTest() {
        BaseLogger logger = new BaseLogger(MOCK_AGENT, UsageLoggers.urlForDemo().replace("https://", "http://"));
        assertTrue("url matches", logger.getUrl().contains("http://"));
        StringBuilder json = new StringBuilder(64);
        JsonMessage.start(json, "test-http", logger.getAgent(), logger.getVersion(), System.currentTimeMillis());
        JsonMessage.stop(json);
        assertTrue("submit succeeds", logger.submit(json.toString()));
    }

    @Test
    public void submitsToDeniedUrlAndFailsTest() {
        for (String url : URLS_DENIED) {
            BaseLogger logger = new BaseLogger(MOCK_AGENT, url);
            assertTrue("logger enabled", logger.isEnabled());
            assertTrue("submit fails", !logger.submit("{}"));
        }
    }

    @Test
    public void submitsToQueueTest() {
        List<String> queue = new ArrayList<>();
        BaseLogger logger = new BaseLogger(MOCK_AGENT, queue);
        assertTrue("url is null", logger.getUrl() == null);
        assertTrue("logger enabled", logger.isEnabled());
        assertTrue("queue size is 0", queue.size() == 0);
        assertTrue("submit succeeds", logger.submit("{}"));
        assertTrue("queue size is 1", queue.size() == 1);
        assertTrue("submit succeeds", logger.submit("{}"));
        assertTrue("queue size is 2", queue.size() == 2);
    }

}
