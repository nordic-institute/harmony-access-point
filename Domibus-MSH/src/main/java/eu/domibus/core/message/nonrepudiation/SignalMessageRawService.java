package eu.domibus.core.message.nonrepudiation;

import eu.domibus.api.model.SignalMessage;
import eu.domibus.api.model.SignalMessageRaw;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignalMessageRawService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SignalMessageRawService.class);

    protected SignalMessageDao signalMessageDao;
    protected SignalMessageRawEnvelopeDao signalMessageRawEnvelopeDao;

    public SignalMessageRawService(SignalMessageDao signalMessageDao, SignalMessageRawEnvelopeDao signalMessageRawEnvelopeDao) {
        this.signalMessageDao = signalMessageDao;
        this.signalMessageRawEnvelopeDao = signalMessageRawEnvelopeDao;
    }

    @Transactional
    public void saveSignalMessageRawService(String rawXml, Long signalMessageId) {
        final SignalMessage signalMessage = signalMessageDao.findByReference(signalMessageId);
        SignalMessageRaw byReference = signalMessageRawEnvelopeDao.findByReference(signalMessageId);
        if (byReference == null) {
            SignalMessageRaw signalMessageRaw = new SignalMessageRaw();
            signalMessageRaw.setRawXML(rawXml);
            signalMessageRaw.setSignalMessage(signalMessage);
            signalMessageRawEnvelopeDao.create(signalMessageRaw);
        } else {
            LOG.warn("SignalMessageRaw already exists for ID_PK: [{}]", signalMessageId);
        }
    }
}
