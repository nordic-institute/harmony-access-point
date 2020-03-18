package eu.domibus.plugin.webService.logging;

import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.ext.logging.event.EventType;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Catalin Enache
 * @since 4.1.4
 */
@RunWith(JMockit.class)
public class WSPluginLoggingEventSenderTest {

    @Injectable
    WSPluginLoggingEventHelper wsPluginLoggingEventHelper;

    @Tested
    WSPluginLoggingEventSender wsPluginLoggingEventSender;


    @Test
    public void test_getLogMessage(final @Mocked LogEvent logEvent) {
        new Expectations(wsPluginLoggingEventSender) {{
            wsPluginLoggingEventSender.checkIfStripPayloadPossible();
            result = true;

            logEvent.getType();
            result = EventType.REQ_IN;
        }};

        //tested method
        wsPluginLoggingEventSender.getLogMessage(logEvent);

        new FullVerifications(wsPluginLoggingEventHelper) {{
            wsPluginLoggingEventHelper.stripPayload((LogEvent) any);
        }};
    }

    @Test
    public void test_checkIfStripPayloadPossible(final @Mocked Logger logger) {
        new Expectations() {{
            Deencapsulation.setField(wsPluginLoggingEventSender, "printPayload", true);
            Deencapsulation.setField(wsPluginLoggingEventSender, "printPayload", false);

            new MockUp<LoggerFactory>() {
                @Mock
                public Logger getLogger(String value) {
                    return logger;
                }
            };
            logger.isInfoEnabled();
            result = true;
            result = false;
        }};

        //tested method
        Assert.assertTrue(wsPluginLoggingEventSender.checkIfStripPayloadPossible());
        Assert.assertFalse(wsPluginLoggingEventSender.checkIfStripPayloadPossible());
    }
}