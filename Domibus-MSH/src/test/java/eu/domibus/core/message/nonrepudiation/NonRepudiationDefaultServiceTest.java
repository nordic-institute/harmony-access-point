package eu.domibus.core.message.nonrepudiation;

import eu.domibus.api.model.RawEnvelopeDto;
import eu.domibus.api.model.SignalMessage;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.audit.envers.ModificationType;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.util.SoapUtil;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.soap.SOAPMessage;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@RunWith(JMockit.class)
public class NonRepudiationDefaultServiceTest {

    @Tested
    NonRepudiationDefaultService nonRepudiationService;

    @Injectable
    SignalMessageRawService signalMessageRawService;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected UserMessageRawEnvelopeDao rawEnvelopeLogDao;

    @Injectable
    protected SoapUtil soapUtil;

    @Injectable
    private SignalMessageDao signalMessageDao;

    @Injectable
    private AuditService auditService;

    @Injectable
    private SignalMessageRawEnvelopeDao signalMessageRawEnvelopeDao;

    @Injectable
    private UserMessageDao userMessageDao;


    @Test
    public void getUserMessageEnvelope_noMessage() {
        String messageId = "msgid";
        new Expectations(nonRepudiationService) {{
            nonRepudiationService.getUserMessageById(messageId);
            result = null;
        }};

        String result = nonRepudiationService.getUserMessageEnvelope(messageId);

        assertNull(result);

        new Verifications() {{
            rawEnvelopeLogDao.findUserMessageEnvelopeById(anyLong);
            times = 0;
        }};
    }

    @Test
    public void getUserMessageEnvelope_noMessageEnvelope(@Mocked UserMessage userMessage) {
        String messageId = "msgid";
        new Expectations(nonRepudiationService) {{
            nonRepudiationService.getUserMessageById(messageId);
            result = userMessage;

            rawEnvelopeLogDao.findUserMessageEnvelopeById(userMessage.getEntityId());
            result = null;
        }};

        String result = nonRepudiationService.getUserMessageEnvelope(messageId);

        assertNull(result);

        new Verifications() {{
            auditService.addMessageEnvelopesDownloadedAudit(messageId, ModificationType.USER_MESSAGE_ENVELOPE_DOWNLOADED);
            times = 0;
        }};
    }

    @Test
    public void getUserMessageEnvelope_ok(@Injectable UserMessage userMessage, @Injectable RawEnvelopeDto rawEnvelopeDto) {
        String messageId = "msgid", envelopContent = "content";

        new Expectations(nonRepudiationService) {{
            nonRepudiationService.getUserMessageById(messageId);
            result = userMessage;

            rawEnvelopeLogDao.findUserMessageEnvelopeById(userMessage.getEntityId());
            result = rawEnvelopeDto;

            rawEnvelopeDto.getRawXmlMessage();
            result = envelopContent;
        }};

        String result = nonRepudiationService.getUserMessageEnvelope(messageId);

        assertEquals(envelopContent, result);

        new Verifications() {{
            auditService.addMessageEnvelopesDownloadedAudit(messageId, ModificationType.USER_MESSAGE_ENVELOPE_DOWNLOADED);
            times = 1;
        }};
    }

    @Test
    public void getSignalMessageEnvelope_noMessage() {
        String userMessageId = "msgid";

        new Expectations() {{
            signalMessageRawEnvelopeDao.findSignalMessageByUserMessageId(userMessageId);
            result = null;
        }};
        String result = nonRepudiationService.getSignalMessageEnvelope(userMessageId);

        assertNull(result);

        new Verifications() {{
            auditService.addMessageEnvelopesDownloadedAudit(userMessageId, ModificationType.SIGNAL_MESSAGE_ENVELOPE_DOWNLOADED);
            times = 0;
        }};
    }

    @Test
    public void getSignalMessageEnvelope_ok(@Injectable RawEnvelopeDto rawEnvelopeDto) {
        String userMessageId = "msgid";
        String rawXml = "rawXml";

        new Expectations() {{
            signalMessageRawEnvelopeDao.findSignalMessageByUserMessageId(userMessageId);
            result = rawEnvelopeDto;

            rawEnvelopeDto.getRawXmlMessage();
            result = rawXml;
        }};
        String result = nonRepudiationService.getSignalMessageEnvelope(userMessageId);

        assertEquals(rawXml, result);

        new Verifications() {{
            auditService.addMessageEnvelopesDownloadedAudit(userMessageId, ModificationType.SIGNAL_MESSAGE_ENVELOPE_DOWNLOADED);
            times = 1;
        }};
    }

    @Test
    public void getMessageEnvelopes() {
        String userMessageId = "msgid", userMessageEnvelope = "userMessageEnvelope", signalEnvelope = "signalEnvelope";
        new Expectations(nonRepudiationService) {{
            nonRepudiationService.getUserMessageEnvelope(userMessageId);
            result = userMessageEnvelope;

            nonRepudiationService.getSignalMessageEnvelope(userMessageId);
            result = signalEnvelope;
        }};

        Map<String, InputStream> result = nonRepudiationService.getMessageEnvelopes(userMessageId);

        assertNotNull(result.get("user_message_envelope.xml"));
        assertNotNull(result.get("signal_message_envelope.xml"));
        assertEquals(2, result.size());
    }
}