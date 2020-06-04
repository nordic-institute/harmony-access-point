package eu.domibus.core.message.attempt;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SEND_MESSAGE_ATTEMPT_AUDIT_ACTIVE;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class MessageAttemptDefaultService implements MessageAttemptService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageAttemptDefaultService.class);

    @Autowired
    MessageAttemptDao messageAttemptDao;

    @Autowired
    DomainCoreConverter domainCoreConverter;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Override
    public List<MessageAttempt> getAttemptsHistory(String messageId) {
        final List<MessageAttemptEntity> entities = messageAttemptDao.findByMessageId(messageId);
        return domainCoreConverter.convert(entities, MessageAttempt.class);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void create(MessageAttempt attempt) {
        if (isMessageAttemptAuditDisabled()) {
            return;
        }

        final MessageAttemptEntity entity = domainCoreConverter.convert(attempt, MessageAttemptEntity.class);
        messageAttemptDao.create(entity);
    }

    @Override
    public void createAndUpdateEndDate(MessageAttempt attempt) {
        LOG.debug("Updating and creating message attempt");

        attempt.setEndDate(new Timestamp(System.currentTimeMillis()));
        create(attempt);//NOSONAR the @Transactional annotation will not be taken into account because this method is called from inside the same class
    }

    protected boolean isMessageAttemptAuditDisabled() {
        return !domibusPropertyProvider.getBooleanProperty(DOMIBUS_SEND_MESSAGE_ATTEMPT_AUDIT_ACTIVE);
    }
}
