package eu.domibus.core.logging.cxf;

import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

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

        }};

        //tested method
        Assert.assertFalse(domibusLoggingEventSender.checkIfStripPayloadPossible());
    }
}