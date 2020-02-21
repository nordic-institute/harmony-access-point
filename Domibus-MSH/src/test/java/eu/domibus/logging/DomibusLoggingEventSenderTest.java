package eu.domibus.logging;

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
public class DomibusLoggingEventSenderTest {

    @Tested
    DomibusLoggingEventSender domibusLoggingEventSender;

    @Injectable
    DomibusLoggingEventHelper domibusLoggingEventHelper;

    @Test
    public void test_getLogMessage(final @Mocked LogEvent logEvent) {
        new Expectations(domibusLoggingEventSender) {{
            domibusLoggingEventSender.checkIfStripPayloadPossible();
            result = true;

            logEvent.getType();
            result = EventType.RESP_OUT;
        }};

        //tested method
        domibusLoggingEventSender.getLogMessage(logEvent);

        new FullVerifications(domibusLoggingEventSender) {{
            domibusLoggingEventHelper.stripPayload((LogEvent) any);
        }};
    }

    @Test
    public void test_checkIfStripPayloadPossible(final @Mocked Logger logger) {
        new Expectations() {{
            Deencapsulation.setField(domibusLoggingEventSender, "printPayload", true);
            Deencapsulation.setField(domibusLoggingEventSender, "printPayload", false);

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
        Assert.assertTrue(domibusLoggingEventSender.checkIfStripPayloadPossible());
        Assert.assertFalse(domibusLoggingEventSender.checkIfStripPayloadPossible());
    }
}