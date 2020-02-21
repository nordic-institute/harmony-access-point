package eu.domibus.logging;

import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.ext.logging.event.EventType;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.apache.cxf.helpers.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class DomibusLoggingEventHelperImplTest {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusLoggingEventHelperImplTest.class);

    @Tested
    DomibusLoggingEventHelperImpl domibusLoggingEventHelper;

    private static void logInfo(String test, String methodName, long before) {
        LOG.info("Test {}, method {} has spent {} milliseconds",
                test, methodName, System.currentTimeMillis() - before);
    }

    @Test
    public void test_stripPayload_SendMessage(final @Mocked LogEvent logEvent) throws Exception {
        final String payload = readPayload("payload_SendMessage.xml");
        new Expectations() {{
            logEvent.getType();
            result = EventType.REQ_OUT;

            logEvent.getOperationName();
            result = "test Invoke";

            logEvent.isMultipartContent();
            result = true;

            logEvent.getPayload();
            result = payload;
        }};

        //tested method
        long before = System.currentTimeMillis();
        domibusLoggingEventHelper.stripPayload(logEvent);
        logInfo("test_stripPayload_SendMessage", "stripPayload", before);

        new Verifications() {{
            final String payloadActual;
            logEvent.setPayload(payloadActual = withCapture());
            Assert.assertNotNull(payloadActual);
            Assert.assertTrue(payloadActual.split(DomibusLoggingEventHelperImpl.CONTENT_TYPE_MARKER).length == 2);
        }};
    }

    @Test
    public void checkIfOperationIsAllowed(final @Mocked LogEvent logEvent) {
        new Expectations() {{
            logEvent.isMultipartContent();
            result = true;

            logEvent.getType();
            result = EventType.REQ_OUT;
        }};


        //tested method
        Assert.assertTrue(domibusLoggingEventHelper.checkIfOperationIsAllowed(logEvent));
    }


    @Test
    public void test_stripPayload(final @Mocked LogEvent logEvent) {
        new Expectations() {{
            logEvent.getOperationName();
            result = "test";

            domibusLoggingEventHelper.checkIfOperationIsAllowed(logEvent);
            result = false;
        }};

        //tested method
        domibusLoggingEventHelper.stripPayload(logEvent);

        new FullVerifications(domibusLoggingEventHelper) {{

        }};
    }

    @Test
    public void testCheckIfOperationIsAllowed() {
    }

    private String readPayload(final String payloadName) throws Exception {
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream("eu/domibus/logging/" + payloadName), "UTF-8");
    }

}