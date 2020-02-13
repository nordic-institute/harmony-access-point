package eu.domibus.logging;

import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.ext.logging.AbstractLoggingInterceptor;
import org.apache.cxf.ext.logging.event.EventType;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.apache.cxf.helpers.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Catalin Enache
 * @since 4.1.1
 */
@RunWith(JMockit.class)
public class DomibusLoggingEventHelperTest {

    @Tested
    DomibusLoggingEventHelper domibusLoggingEventHelper;

    @Test
    public void test_stripPayload(final @Mocked LogEvent logEvent) throws Exception {
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
        domibusLoggingEventHelper.stripPayload(logEvent);

        new Verifications() {{
            final String payloadActual;
            logEvent.setPayload(payloadActual = withCapture());
            Assert.assertNotNull(payloadActual);
            Assert.assertTrue(payloadActual.split(DomibusLoggingEventHelper.CONTENT_TYPE_MARKER).length == 2);
        }};
    }

    @Test
    public void test_stripPayload_SubmitMessage(final @Mocked LogEvent logEvent) throws Exception {

        final String payload = readPayload("payload_SubmitMessage.xml");

        new Expectations() {{
            logEvent.getType();
            result = EventType.REQ_IN;

            logEvent.getOperationName();
            result = "test submitMessage";

            logEvent.getPayload();
            result = payload;
        }};

        domibusLoggingEventHelper.stripPayload(logEvent);

        new Verifications() {{
            final String actualPayload;
            logEvent.setPayload(actualPayload = withCapture() );
            Assert.assertNotNull(actualPayload);
            Assert.assertTrue(actualPayload.contains(AbstractLoggingInterceptor.CONTENT_SUPPRESSED));
        }};
    }

    @Test
    public void test_stripPayload_SubmitMessage_MultipleValues(final @Mocked LogEvent logEvent) throws Exception {

        final String payload = readPayload("payload_SubmitMessage_MultiplePayloads.xml");

        new Expectations() {{
            logEvent.getType();
            result = EventType.REQ_IN;

            logEvent.getOperationName();
            result = "test submitMessage";

            logEvent.isMultipartContent();
            result = false;

            logEvent.getPayload();
            result = payload;
        }};

        domibusLoggingEventHelper.stripPayload(logEvent);

        new FullVerifications() {{
            final String actualPayload;
            logEvent.setPayload(actualPayload = withCapture());
            Assert.assertNotNull(actualPayload);
            Assert.assertEquals(3, StringUtils.countMatches(actualPayload, AbstractLoggingInterceptor.CONTENT_SUPPRESSED));
        }};
    }

    @Test
    public void test_stripPayload_RetrieveMessage(final @Mocked LogEvent logEvent) throws Exception {

        final String payload = readPayload("payload_RetrieveMessage.xml");

        new Expectations() {{
            logEvent.getType();
            result = EventType.RESP_OUT;

            logEvent.getOperationName();
            result = "test retrieveMessage";

            logEvent.getPayload();
            result = payload;
        }};

        domibusLoggingEventHelper.stripPayload(logEvent);

        new Verifications() {{
            final String actualPayload;
            logEvent.setPayload(actualPayload = withCapture() );
            Assert.assertNotNull(actualPayload);
            Assert.assertTrue(actualPayload.contains(AbstractLoggingInterceptor.CONTENT_SUPPRESSED));
        }};
    }

    @Test
    public void test_stripPayload_SubmitMessage_NoContent(final @Mocked LogEvent logEvent) throws Exception {

        final String payload = readPayload("payload_SubmitMessage_no_content.xml");

        new Expectations() {{
            logEvent.getType();
            result = EventType.REQ_IN;

            logEvent.getOperationName();
            result = "test submitMessage";

            logEvent.isMultipartContent();
            result = false;

            logEvent.getPayload();
            result = payload;
        }};

        domibusLoggingEventHelper.stripPayload(logEvent);

        new Verifications() {{
            String actualPayload;
            logEvent.setPayload(actualPayload = withCapture());
            Assert.assertEquals(payload, actualPayload);
            times=1;
        }};
    }

    private String readPayload(final String payloadName) throws Exception {
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(payloadName), "UTF-8");
    }

}