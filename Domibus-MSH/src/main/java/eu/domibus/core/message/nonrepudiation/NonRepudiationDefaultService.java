package eu.domibus.core.message.nonrepudiation;

import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.audit.envers.ModificationType;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.common.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_NONREPUDIATION_AUDIT_ACTIVE;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class NonRepudiationDefaultService implements NonRepudiationService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(NonRepudiationDefaultService.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected RawEnvelopeLogDao rawEnvelopeLogDao;

    @Autowired
    protected SoapUtil soapUtil;

    @Autowired
    private SignalMessageDao signalMessageDao;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private AuditService auditService;

    @Override
    public void saveRequest(SOAPMessage request, UserMessage userMessage) {
        if (isNonRepudiationAuditDisabled()) {
            return;
        }

        try {
            String rawXMLMessage = soapUtil.getRawXMLMessage(request);
            LOG.debug("Persist raw XML envelope: " + rawXMLMessage);
            RawEnvelopeLog rawEnvelopeLog = new RawEnvelopeLog();
            if (userMessage != null) {
                rawEnvelopeLog.setMessageId(userMessage.getMessageInfo().getMessageId());
            }
            rawEnvelopeLog.setRawXML(rawXMLMessage);
            rawEnvelopeLog.setUserMessage(userMessage);
            rawEnvelopeLogDao.create(rawEnvelopeLog);
        } catch (TransformerException e) {
            LOG.warn("Unable to log the raw message XML due to: ", e);
        }
    }

    @Override
    public void saveResponse(SOAPMessage response, SignalMessage signalMessage) {
        if (isNonRepudiationAuditDisabled()) {
            return;
        }

        try {
            String rawXMLMessage = soapUtil.getRawXMLMessage(response);
            LOG.debug("Persist raw XML envelope: " + rawXMLMessage);
            RawEnvelopeLog rawEnvelopeLog = new RawEnvelopeLog();
            rawEnvelopeLog.setRawXML(rawXMLMessage);
            rawEnvelopeLog.setSignalMessage(signalMessage);
            rawEnvelopeLogDao.create(rawEnvelopeLog);
        } catch (TransformerException e) {
            LOG.warn("Unable to log the raw message XML due to: ", e);
        }
    }

    @Override
    public void saveResponse(SOAPMessage response, String userMessageId) {
        if (isNonRepudiationAuditDisabled()) {
            return;
        }

        List<SignalMessage> signalMessages = signalMessageDao.findSignalMessagesByRefMessageId(userMessageId);
        if (CollectionUtils.isEmpty(signalMessages)) {
            LOG.error("Could not find any signal message for ref message [{}]", userMessageId);
            return;
        }

        SignalMessage signalMessage = signalMessages.stream().findFirst().get();
        saveResponse(response, signalMessage);
    }


    @Override
    public String getUserMessageEnvelope(String messageId) {
        UserMessage userMessage = getUserMessageById(messageId);
        if (userMessage == null) {
            LOG.info("User message with id [{}] was not found.", messageId);
            return null;
        }

        RawEnvelopeDto userEnvelope = rawEnvelopeLogDao.findUserMessageEnvelopeById(userMessage.getEntityId());
        if (userEnvelope == null) {
            LOG.info("User message envelope with entity id [{}] was not found.", userMessage.getEntityId());
            return null;
        }

        auditService.addMessageEnvelopesDownloadedAudit(messageId, ModificationType.USER_MESSAGE_ENVELOPE_DOWNLOADED);
        LOG.debug("Returning the user message envelope with id [{}]: [{}]", messageId, userEnvelope.getRawMessage());
        return userEnvelope.getRawMessage();
    }

    @Override
    public String getSignalMessageEnvelope(String userMessageId) {
        SignalMessage signalMessage = messagingDao.findSignalMessageByUserMessageId(userMessageId);
        if (signalMessage == null) {
            LOG.info("Signal message with corresponding user message id [{}] was not found.", userMessageId);
            return null;
        }
        if (signalMessage.getRawEnvelopeLog() == null) {
            LOG.info("Signal message raw envelope with corresponding user message id [{}] was not found.", userMessageId);
            return null;
        }

        auditService.addMessageEnvelopesDownloadedAudit(userMessageId, ModificationType.SIGNAL_MESSAGE_ENVELOPE_DOWNLOADED);
        LOG.debug("Returning the signal message envelope with user message id [{}]: [{}]", userMessageId, signalMessage.getRawEnvelopeLog().getRawXML());
        return signalMessage.getRawEnvelopeLog().getRawXML();
    }

    @Override
    public Map<String, InputStream> getMessageEnvelopes(String messageId) {
        Map<String, InputStream> result = new HashMap<>();

        String userMessageEnvelope = getUserMessageEnvelope(messageId);
        if (userMessageEnvelope != null) {
            InputStream userEnvelopeStream = new ByteArrayInputStream(userMessageEnvelope.getBytes());
            result.put("user_message_envelope.xml", userEnvelopeStream);
            IOUtils.closeQuietly(userEnvelopeStream);
        }

        String signalEnvelope = getSignalMessageEnvelope(messageId);
        if (signalEnvelope != null) {
            InputStream signalEnvelopeStream = new ByteArrayInputStream(signalEnvelope.getBytes());
            result.put("signal_message_envelope.xml", signalEnvelopeStream);
            IOUtils.closeQuietly(signalEnvelopeStream);
        }

        return result;
    }

    protected UserMessage getUserMessageById(String messageId) throws MessageNotFoundException {
        UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        if (userMessage == null) {
            throw new MessageNotFoundException("Could not find message metadata for id " + messageId);
        }

        return userMessage;
    }



    protected boolean isNonRepudiationAuditDisabled() {
        return !domibusPropertyProvider.getBooleanProperty(DOMIBUS_NONREPUDIATION_AUDIT_ACTIVE);
    }
}
