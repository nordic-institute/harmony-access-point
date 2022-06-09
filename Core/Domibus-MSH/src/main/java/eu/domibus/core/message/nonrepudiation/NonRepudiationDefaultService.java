package eu.domibus.core.message.nonrepudiation;

import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.model.RawEnvelopeDto;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageRaw;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.audit.envers.ModificationType;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_NONREPUDIATION_AUDIT_ACTIVE;

/**
 * @author Cosmin Baciu
 * @author Ion Perpegel
 * @since 3.3
 */
@Service
public class NonRepudiationDefaultService implements NonRepudiationService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(NonRepudiationDefaultService.class);
    public static final String PERSIST_RAW_XML_ENVELOPE = "Persist raw XML envelope: [{}]";
    public static final String NON_REPUDIATION_AUDIT_IS_DISABLED_SKIP_SAVING_NON_REPUDIATION_DATA = "Non Repudiation Audit is disabled, skip saving non-repudiation data.";

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected UserMessageRawEnvelopeDao rawEnvelopeLogDao;

    @Autowired
    protected SignalMessageRawEnvelopeDao signalMessageRawEnvelopeDao;

    @Autowired
    protected SoapUtil soapUtil;

    @Autowired
    private UserMessageDao userMessageDao;

    @Autowired
    private AuditService auditService;


    @Autowired
    protected SignalMessageRawService signalMessageRawService;

    @Override
    public void saveRawEnvelope(String rawXMLMessage, UserMessage userMessage) {
        if (isNonRepudiationAuditDisabled()) {
            LOG.debug(NON_REPUDIATION_AUDIT_IS_DISABLED_SKIP_SAVING_NON_REPUDIATION_DATA);
            return;
        }

        LOG.debug(PERSIST_RAW_XML_ENVELOPE, rawXMLMessage);
        UserMessageRaw rawEnvelopeLog = new UserMessageRaw();
        rawEnvelopeLog.setUserMessage(userMessageDao.findByReference(userMessage.getEntityId()));
        rawEnvelopeLog.setRawXML(rawXMLMessage);
        rawEnvelopeLogDao.create(rawEnvelopeLog);
    }

    @Override
    public UserMessageRaw createUserMessageRaw(SOAPMessage request) throws TransformerException {
        String rawXMLMessage = soapUtil.getRawXMLMessage(request);
        UserMessageRaw rawEnvelopeLog = new UserMessageRaw();
        rawEnvelopeLog.setRawXML(rawXMLMessage);
        return rawEnvelopeLog;
    }

    @Override
    public void saveRequest(SOAPMessage request, UserMessage userMessage) {
        if (isNonRepudiationAuditDisabled()) {
            LOG.debug(NON_REPUDIATION_AUDIT_IS_DISABLED_SKIP_SAVING_NON_REPUDIATION_DATA);
            return;
        }

        try {
            String rawXMLMessage = soapUtil.getRawXMLMessage(request);
            LOG.debug(PERSIST_RAW_XML_ENVELOPE, rawXMLMessage);
            UserMessageRaw rawEnvelopeLog = new UserMessageRaw();
            if (userMessage != null) {
                rawEnvelopeLog.setUserMessage(userMessageDao.findByReference(userMessage.getEntityId()));
            }
            rawEnvelopeLog.setRawXML(rawXMLMessage);
            rawEnvelopeLogDao.create(rawEnvelopeLog);
        } catch (TransformerException e) {
            LOG.warn("Unable to log the raw message XML due to: ", e);
        }
    }

    @Override
    public void saveResponse(SOAPMessage response, Long signalMessageEntityId) {
        if (isNonRepudiationAuditDisabled()) {
            LOG.debug(NON_REPUDIATION_AUDIT_IS_DISABLED_SKIP_SAVING_NON_REPUDIATION_DATA);
            return;
        }

        try {
            String rawXMLMessage = soapUtil.getRawXMLMessage(response);
            LOG.debug(PERSIST_RAW_XML_ENVELOPE, rawXMLMessage);
            signalMessageRawService.saveSignalMessageRawService(rawXMLMessage, signalMessageEntityId);
        } catch (TransformerException e) {
            LOG.warn("Unable to log the raw message XML due to: ", e);
        }
    }

    @Override
    public String getUserMessageEnvelope(String messageId) {
        UserMessage userMessage = getUserMessageById(messageId);

        RawEnvelopeDto rawEnvelopeDto = rawEnvelopeLogDao.findUserMessageEnvelopeById(userMessage.getEntityId());
        if (rawEnvelopeDto == null) {
            LOG.info("User message envelope with entity id [{}] was not found.", userMessage.getEntityId());
            return null;
        }

        auditService.addMessageEnvelopesDownloadedAudit(messageId, ModificationType.USER_MESSAGE_ENVELOPE_DOWNLOADED);
        LOG.debug("Returning the user message envelope with id [{}]: [{}]", messageId, rawEnvelopeDto.getRawMessage());
        return rawEnvelopeDto.getRawXmlMessage();
    }

    @Override
    public String getSignalMessageEnvelope(String userMessageId) {
        RawEnvelopeDto rawEnvelopeDto = signalMessageRawEnvelopeDao.findSignalMessageByUserMessageId(userMessageId);
        if (rawEnvelopeDto == null) {
            if (userMessageDao.findByMessageId(userMessageId) == null) {
                throw new MessageNotFoundException(userMessageId);
            }
            LOG.info("Signal message with corresponding user message id [{}] was not found.", userMessageId);
            return null;
        }

        auditService.addMessageEnvelopesDownloadedAudit(userMessageId, ModificationType.SIGNAL_MESSAGE_ENVELOPE_DOWNLOADED);
        LOG.debug("Returning the signal message envelope with user message id [{}]: [{}]", userMessageId, rawEnvelopeDto.getRawMessage());
        return rawEnvelopeDto.getRawXmlMessage();
    }

    @Override
    public Map<String, InputStream> getMessageEnvelopes(String messageId) {
        Map<String, InputStream> result = new HashMap<>();

        String userMessageEnvelope = getUserMessageEnvelope(messageId);
        if (userMessageEnvelope != null) {
            try (InputStream userEnvelopeStream = new ByteArrayInputStream(userMessageEnvelope.getBytes())) {
                result.put("user_message_envelope.xml", userEnvelopeStream);
            } catch (IOException e) {
                LOG.debug("Error creating stream for the user message raw envelope with corresponding user message id [{}].", messageId);
            }
        }

        String signalEnvelope = getSignalMessageEnvelope(messageId);
        if (signalEnvelope != null) {
            try (InputStream signalEnvelopeStream = new ByteArrayInputStream(signalEnvelope.getBytes())) {
                result.put("signal_message_envelope.xml", signalEnvelopeStream);
            } catch (IOException e) {
                LOG.debug("Error creating stream for the signal message raw envelope with corresponding user message id [{}].", messageId);
            }
        }

        return result;
    }

    protected UserMessage getUserMessageById(String messageId) throws MessageNotFoundException {
        UserMessage userMessage = userMessageDao.findByMessageId(messageId);
        if (userMessage == null) {
            throw new MessageNotFoundException(messageId);
        }

        return userMessage;
    }

    protected boolean isNonRepudiationAuditDisabled() {
        return !domibusPropertyProvider.getBooleanProperty(DOMIBUS_NONREPUDIATION_AUDIT_ACTIVE);
    }
}
