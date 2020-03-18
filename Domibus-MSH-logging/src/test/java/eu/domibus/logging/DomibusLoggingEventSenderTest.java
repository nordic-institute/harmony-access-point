package eu.domibus.logging;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.apache.cxf.helpers.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Catalin Enache
 * @since 4.1.1
 */
@RunWith(JMockit.class)
public class DomibusLoggingEventSenderTest {

    @Tested
    DomibusLoggingEventSender domibusLoggingEventSender;

    @Test
    public void test_stripPayload(final @Mocked LogEvent logEvent) throws Exception {
        final String payload = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("payload_SendMessage.xml"), "UTF-8");

        new Expectations() {{
            logEvent.isMultipartContent();
            result = true;

            logEvent.getPayload();
            result = payload;
        }};

        //tested method
        domibusLoggingEventSender.stripPayload(logEvent);

        new Verifications() {{
            final String payloadActual;
            logEvent.setPayload(payloadActual = withCapture());
            Assert.assertNotNull(payloadActual);
            Assert.assertTrue(payloadActual.split(DomibusLoggingEventSender.CONTENT_TYPE).length == 2);
        }};
    }

    @Test
    public void test_stripHeaders(final @Mocked LogEvent event) {
        Map<String, String> headers = new HashMap<>();
        headers.put(DomibusLoggingEventSender.HEADERS_AUTHORIZATION, "Basic test 123");
        headers.put("host", "localhost:8080");
        headers.put("content-type", "application/soap+xml;charset=UTF-8");

        new Expectations() {{
            event.getHeaders();
            result = headers;
        }};

        //tested method
        domibusLoggingEventSender.stripHeaders(event);
        Assert.assertNotNull(event.getHeaders());
        Assert.assertNull(event.getHeaders().get(DomibusLoggingEventSender.HEADERS_AUTHORIZATION));
    }
}