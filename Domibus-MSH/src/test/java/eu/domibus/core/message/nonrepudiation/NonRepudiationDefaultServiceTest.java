package eu.domibus.core.message.nonrepudiation;

import eu.domibus.api.model.RawEnvelopeDto;
import eu.domibus.api.model.RawEnvelopeLog;
import eu.domibus.api.model.SignalMessage;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.audit.envers.ModificationType;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.util.SoapUtil;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.soap.SOAPMessage;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@RunWith(JMockit.class)
public class NonRepudiationDefaultServiceTest {

    @Tested
    NonRepudiationDefaultService nonRepudiationService;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected RawEnvelopeLogDao rawEnvelopeLogDao;

    @Injectable
    protected SoapUtil soapUtil;

    @Injectable
    private SignalMessageDao signalMessageDao;

    @Injectable
    private MessagingDao messagingDao;

    @Injectable
    private AuditService auditService;

    @Test
    public void saveResponse_disabled(@Mocked SOAPMessage response) {
        String userMessageId = "msgid";
        new Expectations(nonRepudiationService) {{
            nonRepudiationService.isNonRepudiationAuditDisabled();
            result = true;
        }};

        nonRepudiationService.saveResponse(response, userMessageId);

        new Verifications() {{
            signalMessageDao.findSignalMessagesByRefMessageId(userMessageId);
            times = 0;
        }};
    }

    @Test
    public void saveResponse_noMessage(@Mocked SOAPMessage response) {
        String userMessageId = "msgid";
        new Expectations(nonRepudiationService) {{
            nonRepudiationService.isNonRepudiationAuditDisabled();
            result = false;

            signalMessageDao.findSignalMessagesByRefMessageId(userMessageId);
            result = Arrays.asList();
        }};

        nonRepudiationService.saveResponse(response, userMessageId);

        new Verifications() {{
            nonRepudiationService.saveResponse(response, (SignalMessage) any);
            times = 0;
        }};
    }

    @Test
    public void saveResponse_ok(@Mocked SOAPMessage response, @Mocked SignalMessage signalMessage) {
        String userMessageId = "msgid";
        List<SignalMessage> signalMessages = Arrays.asList(signalMessage);
        new Expectations(nonRepudiationService) {{
            nonRepudiationService.isNonRepudiationAuditDisabled();
            result = false;

            signalMessageDao.findSignalMessagesByRefMessageId(userMessageId);
            result = signalMessages;

            signalMessages.stream().findFirst();
            result = null;
        }};

        nonRepudiationService.saveResponse(response, userMessageId);

        new Verifications() {{
            nonRepudiationService.saveResponse(response, signalMessage);
            times = 1;
        }};
    }

    @Test
    public void getUserMessageEnvelope_noMessage() {
        String messageId = "msgid";
        new Expectations(nonRepudiationService) {{
            nonRepudiationService.getUserMessageById(messageId);
            result = null;
        }};

        String result = nonRepudiationService.getUserMessageEnvelope(messageId);

        assertEquals(null, result);

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

        assertEquals(null, result);

        new Verifications() {{
            auditService.addMessageEnvelopesDownloadedAudit(messageId, ModificationType.USER_MESSAGE_ENVELOPE_DOWNLOADED);
            times = 0;
        }};
    }

    @Test
    public void getUserMessageEnvelope_ok(@Mocked UserMessage userMessage, @Mocked RawEnvelopeDto rawEnvelopeDto) {
        String messageId = "msgid", envelopContent = "content";

        new Expectations(nonRepudiationService) {{
            nonRepudiationService.getUserMessageById(messageId);
            result = userMessage;

            rawEnvelopeLogDao.findUserMessageEnvelopeById(userMessage.getEntityId());
            result = rawEnvelopeDto;

            rawEnvelopeDto.getRawMessage();
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
        new Expectations(nonRepudiationService) {{
            messagingDao.findSignalMessageByUserMessageId(userMessageId);
            result = null;
        }};

        String result = nonRepudiationService.getSignalMessageEnvelope(userMessageId);

        assertEquals(null, result);

        new Verifications() {{
            auditService.addMessageEnvelopesDownloadedAudit(userMessageId, ModificationType.SIGNAL_MESSAGE_ENVELOPE_DOWNLOADED);
            times = 0;
        }};
    }

    @Test
    public void getSignalMessageEnvelope_noMessageEnvelope(@Mocked SignalMessage signalMessage) {
        String userMessageId = "msgid";
        new Expectations(nonRepudiationService) {{
            messagingDao.findSignalMessageByUserMessageId(userMessageId);
            result = signalMessage;

            signalMessage.getRawEnvelopeLog();
            result = null;
        }};

        String result = nonRepudiationService.getSignalMessageEnvelope(userMessageId);

        assertEquals(null, result);

        new Verifications() {{
            auditService.addMessageEnvelopesDownloadedAudit(userMessageId, ModificationType.SIGNAL_MESSAGE_ENVELOPE_DOWNLOADED);
            times = 0;
        }};
    }

    @Test
    public void getSignalMessageEnvelope_ok(@Mocked SignalMessage signalMessage, @Mocked RawEnvelopeLog rawEnvelopeLog) {
        String userMessageId = "msgid", rawXml = "rawXml";
        new Expectations(nonRepudiationService) {{
            messagingDao.findSignalMessageByUserMessageId(userMessageId);
            result = signalMessage;

            signalMessage.getRawEnvelopeLog();
            result = rawEnvelopeLog;

            rawEnvelopeLog.getRawXML();
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