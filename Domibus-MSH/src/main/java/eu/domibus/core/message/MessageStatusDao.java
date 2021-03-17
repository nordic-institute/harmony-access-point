package eu.domibus.core.message;

import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.MessageStatusEntity;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.core.dao.ListDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import javax.persistence.TypedQuery;


/**
 * @param <F> MessageLog type: either UserMessageLog or SignalMessageLog
 * @author Federico Martini
 * @since 3.2
 */
public abstract class MessageStatusDao<F extends MessageStatusEntity> extends ListDao<F> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageLog.class);

    public MessageStatusDao(final Class<F> type) {
        super(type);
    }

    public MessageStatusEntity findMessageStatus(final MessageStatus messageStatus) {
        TypedQuery<MessageStatusEntity> query = em.createNamedQuery("MessageStatusEntity.findByStatus", MessageStatusEntity.class);
        query.setParameter("MESSAGE_STATUS", messageStatus);
        return query.getSingleResult();
    }
}
