package eu.domibus.core.nonrepudiation;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.dao.RawEnvelopeLogDao;
import eu.domibus.common.model.logging.RawEnvelopeLog;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS_NONREPUDIATION_AUDIT_ACTIVE;

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

    @Override
    public void saveRequest(String rawXMLMessage, UserMessage userMessage) {
        if (isNonRepudiationAuditDisabled()) {
            return;
        }

        LOG.debug("Persist raw XML envelope: " + rawXMLMessage);
        RawEnvelopeLog rawEnvelopeLog = new RawEnvelopeLog();
        if (userMessage != null) {
            rawEnvelopeLog.setMessageId(userMessage.getMessageInfo().getMessageId());
        }
        rawEnvelopeLog.setRawXML(rawXMLMessage);
        rawEnvelopeLog.setUserMessage(userMessage);

        rawEnvelopeLogDao.create(rawEnvelopeLog);

    }

    @Override
    public String createNonRepudiation(SOAPMessage request) throws TransformerException {
        return soapUtil.getRawXMLMessage(request);

    }

    @Override
    public void saveResponse(String rawXMLMessage, SignalMessage signalMessage) {
        if (isNonRepudiationAuditDisabled()) {
            return;
        }

        LOG.debug("Persist raw XML envelope: " + rawXMLMessage);
        RawEnvelopeLog rawEnvelopeLog = new RawEnvelopeLog();
        rawEnvelopeLog.setRawXML(rawXMLMessage);
        rawEnvelopeLog.setSignalMessage(signalMessage);
        rawEnvelopeLogDao.create(rawEnvelopeLog);
    }

    protected boolean isNonRepudiationAuditDisabled() {
        return !domibusPropertyProvider.getBooleanProperty(DOMIBUS_NONREPUDIATION_AUDIT_ACTIVE);
    }
}
