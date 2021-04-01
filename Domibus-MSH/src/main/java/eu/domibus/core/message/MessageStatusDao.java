package eu.domibus.core.message;

import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.MessageStatusEntity;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.core.dao.ListDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import javax.persistence.TypedQuery;


/**
 * @author Federico Martini
 * @author Cosmin Baciu
 * @since 3.2
 */
public class MessageStatusDao extends BasicDao<MessageStatusEntity> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageLog.class);

    public MessageStatusDao() {
        super(MessageStatusEntity.class);
    }

    public MessageStatusEntity findMessageStatus(final MessageStatus messageStatus) {
        TypedQuery<MessageStatusEntity> query = em.createNamedQuery("MessageStatusEntity.findByStatus", MessageStatusEntity.class);
        query.setParameter("MESSAGE_STATUS", messageStatus);
        return query.getSingleResult();
    }
}
