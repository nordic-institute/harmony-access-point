package eu.domibus.core.message.nonrepudiation;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.RawEnvelopeDto;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageRaw;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.audit.envers.ModificationType;
import eu.domibus.core.ebms3.receiver.policy.SetPolicyInClientInterceptor;
import eu.domibus.core.ebms3.receiver.policy.SetPolicyInServerInterceptor;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_NONREPUDIATION_AUDIT_ACTIVE;
import static eu.domibus.messaging.MessageConstants.RAW_MESSAGE_XML;

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
    @Transactional
    public void saveUserMessageRawEnvelope(String rawXMLMessage, Long messageEntityId) {
        if (isNonRepudiationAuditDisabled()) {
            LOG.debug(NON_REPUDIATION_AUDIT_IS_DISABLED_SKIP_SAVING_NON_REPUDIATION_DATA);
            return;
        }

        LOG.debug(PERSIST_RAW_XML_ENVELOPE, rawXMLMessage);
        UserMessageRaw rawEnvelopeLog = new UserMessageRaw();
        rawEnvelopeLog.setUserMessage(userMessageDao.findByReference(messageEntityId));
        rawEnvelopeLog.setRawXML(rawXMLMessage);
        rawEnvelopeLogDao.create(rawEnvelopeLog);
    }

    @Override
    public UserMessageRaw createReceivedUserMessageRaw(SOAPMessage request) throws TransformerException {
        String rawXMLMessage = null;
        if (PhaseInterceptorChain.getCurrentMessage() != null && PhaseInterceptorChain.getCurrentMessage().getExchange() != null) {
            //For push
            rawXMLMessage = (String) PhaseInterceptorChain.getCurrentMessage().getExchange().get(RAW_MESSAGE_XML);
        }

        if (StringUtils.isBlank(rawXMLMessage)) {
            //For pull
            rawXMLMessage = SetPolicyInClientInterceptor.RAW_MESSAGE_XML.get();
        }

        if (rawXMLMessage == null) {
            rawXMLMessage = soapUtil.getRawXMLMessage(request);
        }

        UserMessageRaw rawEnvelopeLog = new UserMessageRaw();
        rawEnvelopeLog.setRawXML(rawXMLMessage);
        return rawEnvelopeLog;
    }

    @Override
    @Transactional
    public void saveSignalMessageRawEnvelope(String rawXMLMessage, Long messageEntityId) {
        if (isNonRepudiationAuditDisabled()) {
            LOG.debug(NON_REPUDIATION_AUDIT_IS_DISABLED_SKIP_SAVING_NON_REPUDIATION_DATA);
            return;
        }
        if (StringUtils.isBlank(rawXMLMessage)) {
            LOG.warn("Could not save the raw envelope for signal message with entity id [{}]: raw envelope is null", messageEntityId);
            return;
        }

        LOG.debug(PERSIST_RAW_XML_ENVELOPE, rawXMLMessage);

        try {
            signalMessageRawService.saveSignalMessageRawService(rawXMLMessage, messageEntityId);
        } catch (Exception e) {//a typical error is DataIntegrityViolationException see EDELIVERY-12914
            LOG.error("Could not persist Signal raw envelope for signal message with id [{}]: [{}]", messageEntityId, rawXMLMessage);
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Error saving the signal raw message with id [" + messageEntityId + "]", e);
        }
    }

    @Override
    public String extractRawXMLMessage(SOAPMessage message) {
        try {
            return soapUtil.getRawXMLMessage(message);
        } catch (TransformerException e) {
            LOG.warn("Unable to get the raw message XML", e);
            return null;
        }
    }

    @Override
    public String getUserMessageEnvelope(String messageId, MSHRole mshRole) {
        UserMessage userMessage = getUserMessageById(messageId, mshRole);

        RawEnvelopeDto rawEnvelopeDto = rawEnvelopeLogDao.findUserMessageEnvelopeById(userMessage.getEntityId());
        if (rawEnvelopeDto == null) {
            LOG.debug("User message envelope with entity id [{}] was not found.", userMessage.getEntityId());
            return null;
        }

        auditService.addMessageEnvelopesDownloadedAudit(messageId, ModificationType.USER_MESSAGE_ENVELOPE_DOWNLOADED);
        LOG.debug("Returning the user message envelope with id [{}]: [{}]", messageId, rawEnvelopeDto.getRawXmlMessage());
        return rawEnvelopeDto.getRawXmlMessage();
    }

    @Override
    public String getSignalMessageEnvelope(String userMessageId, MSHRole mshRole) {
        RawEnvelopeDto rawEnvelopeDto = signalMessageRawEnvelopeDao.findSignalMessageByUserMessageId(userMessageId, mshRole);
        if (rawEnvelopeDto == null) {
            if (userMessageDao.findByMessageId(userMessageId, mshRole) == null) {
                throw new MessageNotFoundException(userMessageId);
            }
            LOG.debug("Signal message with corresponding user message id [{}] was not found.", userMessageId);
            return null;
        }

        auditService.addMessageEnvelopesDownloadedAudit(userMessageId, ModificationType.SIGNAL_MESSAGE_ENVELOPE_DOWNLOADED);
        LOG.debug("Returning the signal message envelope with user message id [{}]: [{}]", userMessageId, rawEnvelopeDto.getRawXmlMessage());
        return rawEnvelopeDto.getRawXmlMessage();
    }

    @Override
    public Map<String, InputStream> getMessageEnvelopes(String messageId, MSHRole mshRole) {
        Map<String, InputStream> result = new HashMap<>();

        String userMessageEnvelope = getUserMessageEnvelope(messageId, mshRole);
        if (userMessageEnvelope != null) {
            try (InputStream userEnvelopeStream = new ByteArrayInputStream(userMessageEnvelope.getBytes())) {
                result.put("user_message_envelope.xml", userEnvelopeStream);
            } catch (IOException e) {
                LOG.debug("Error creating stream for the user message raw envelope with corresponding user message id [{}].", messageId);
            }
        }

        String signalEnvelope = getSignalMessageEnvelope(messageId, mshRole);
        if (signalEnvelope != null) {
            try (InputStream signalEnvelopeStream = new ByteArrayInputStream(signalEnvelope.getBytes())) {
                result.put("signal_message_envelope.xml", signalEnvelopeStream);
            } catch (IOException e) {
                LOG.debug("Error creating stream for the signal message raw envelope with corresponding user message id [{}].", messageId);
            }
        }

        return result;
    }

    protected UserMessage getUserMessageById(String messageId, MSHRole mshRole) throws MessageNotFoundException {
        UserMessage userMessage = userMessageDao.findByMessageId(messageId, mshRole);
        if (userMessage == null) {
            throw new MessageNotFoundException(messageId);
        }

        return userMessage;
    }

    protected boolean isNonRepudiationAuditDisabled() {
        return !domibusPropertyProvider.getBooleanProperty(DOMIBUS_NONREPUDIATION_AUDIT_ACTIVE);
    }
}
