package eu.domibus.plugin.webService.logging;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.ext.logging.AbstractLoggingInterceptor;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Catalin Enache
 * @sine 4.1.1
 */
@RunWith(JMockit.class)
public class DomibusWSPluginLoggingEventSenderTest {

    @Tested
    DomibusWSPluginLoggingEventSender domibusWSPluginLoggingEventSender;

    @Test
    public void test_stripPayload_SubmitMessage(final @Mocked LogEvent logEvent) throws Exception {

        final String payload = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("payload_SubmitMessage.xml"), "UTF-8");

        new Expectations() {{
           logEvent.getPayload();
           result = payload;
        }};

        domibusWSPluginLoggingEventSender.stripPayload(logEvent);

        new Verifications() {{
           final String actualPayload;
            logEvent.setPayload(actualPayload = withCapture() );
            Assert.assertNotNull(actualPayload);
            Assert.assertTrue(actualPayload.contains(AbstractLoggingInterceptor.CONTENT_SUPPRESSED));
        }};
    }

    @Test
    public void test_stripPayload_RetrieveMessage(final @Mocked LogEvent logEvent) throws Exception {

        final String payload = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("payload_RetrieveMessage.xml"), "UTF-8");

        new Expectations() {{
            logEvent.getPayload();
            result = payload;
        }};

        domibusWSPluginLoggingEventSender.stripPayload(logEvent);

        new Verifications() {{
            final String actualPayload;
            logEvent.setPayload(actualPayload = withCapture() );
            Assert.assertNotNull(actualPayload);
            Assert.assertTrue(actualPayload.contains(AbstractLoggingInterceptor.CONTENT_SUPPRESSED));
        }};
    }
}