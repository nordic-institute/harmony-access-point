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
public class DomibusLoggingEventHelperImplTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusLoggingEventHelperImplTest.class);

    private static void logInfo(String test, String methodName, long before) {
        LOG.info("Test {}, method {} has spent {} milliseconds",
                test, methodName, System.currentTimeMillis() - before);
    }

    @Tested
    DomibusLoggingEventHelperImpl domibusLoggingEventHelper;

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

        //tested method
        long before = System.currentTimeMillis();
        domibusLoggingEventHelper.stripPayload(logEvent);
        logInfo("test_stripPayload_SubmitMessage", "stripPayload", before);

        new Verifications() {{
            final String actualPayload;
            logEvent.setPayload(actualPayload = withCapture());
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

        //tested method
        long before = System.currentTimeMillis();
        domibusLoggingEventHelper.stripPayload(logEvent);
        logInfo("test_stripPayload_SubmitMessage_MultipleValues", "stripPayload", before);


        new FullVerifications() {{
            final String actualPayload;
            logEvent.setPayload(actualPayload = withCapture());
            Assert.assertNotNull(actualPayload);
            Assert.assertEquals(3, StringUtils.countMatches(actualPayload, AbstractLoggingInterceptor.CONTENT_SUPPRESSED));
        }};
    }

    @Test
    public void test_stripPayload_SubmitMessage_MTOM(final @Mocked LogEvent logEvent) throws Exception {

        final String payload = readPayload("payload_SubmitMessage_MTOM_Attachments.xml");

        new Expectations() {{
            logEvent.getType();
            result = EventType.REQ_IN;

            logEvent.getOperationName();
            result = "{http://org.ecodex.backend/1_1/}submitMessage";

            logEvent.isMultipartContent();
            result = true;

            logEvent.getPayload();
            result = payload;

            logEvent.getContentType();
            result = "multipart/related; type=\"application/xop+xml\"; start=\"<rootpart@soapui.org>\"; start-info=\"application/soap+xml\"; action=\"\"; boundary=\"----=_Part_6_567004613.1582023394958\"";
        }};

        //tested method
        long before = System.currentTimeMillis();
        domibusLoggingEventHelper.stripPayload(logEvent);
        logInfo("test_stripPayload_SubmitMessage_MTOM", "stripPayload", before);


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

        //tested method
        long before = System.currentTimeMillis();
        domibusLoggingEventHelper.stripPayload(logEvent);
        logInfo("test_stripPayload_RetrieveMessage", "stripPayload", before);


        new Verifications() {{
            final String actualPayload;
            logEvent.setPayload(actualPayload = withCapture());
            Assert.assertNotNull(actualPayload);
            Assert.assertTrue(actualPayload.contains(AbstractLoggingInterceptor.CONTENT_SUPPRESSED));
        }};
    }

    @Test
    public void test_stripPayload_RetrieveMessage_2Attachments(final @Mocked LogEvent logEvent) throws Exception {

        final String payload = readPayload("payload_RetrieveMessage_2Attachments.xml");

        new Expectations() {{
            logEvent.getType();
            result = EventType.RESP_OUT;

            logEvent.getOperationName();
            result = "test retrieveMessage";

            logEvent.isMultipartContent();
            result = false;

            logEvent.getPayload();
            result = payload;
        }};

        //tested method
        long before = System.currentTimeMillis();
        domibusLoggingEventHelper.stripPayload(logEvent);
        logInfo("test_stripPayload_RetrieveMessage_2Attachments", "stripPayload", before);


        new Verifications() {{
            final String actualPayload;
            logEvent.setPayload(actualPayload = withCapture());
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

        //tested method
        long before = System.currentTimeMillis();
        domibusLoggingEventHelper.stripPayload(logEvent);
        logInfo("test_stripPayload_SubmitMessage_NoContent",
                "stripPayload", before);


        new Verifications() {{
            String actualPayload;
            logEvent.setPayload(actualPayload = withCapture());
            Assert.assertEquals(payload, actualPayload);
            times = 1;
        }};
    }

    private String readPayload(final String payloadName) throws Exception {
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(payloadName), "UTF-8");
    }

}