package eu.domibus.core.message;

import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.MessageStatusEntity;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Service;

import javax.persistence.TypedQuery;


/**
 * @author Federico Martini
 * @author Cosmin Baciu
 * @since 3.2
 */
@Service
public class MessageStatusDao extends BasicDao<MessageStatusEntity> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageLog.class);

    public MessageStatusDao() {
        super(MessageStatusEntity.class);
    }

    public MessageStatusEntity findOrCreate(MessageStatus messageStatus) {
        MessageStatusEntity messageStatusEntity = findMessageStatus(messageStatus);
        if (messageStatusEntity != null) {
            return messageStatusEntity;
        }
        MessageStatusEntity entity = new MessageStatusEntity();
        entity.setMessageStatus(messageStatus);
        create(entity);
        return entity;
    }

    public MessageStatusEntity findMessageStatus(final MessageStatus messageStatus) {
        TypedQuery<MessageStatusEntity> query = em.createNamedQuery("MessageStatusEntity.findByStatus", MessageStatusEntity.class);
        query.setParameter("MESSAGE_STATUS", messageStatus);
        return DataAccessUtils.singleResult(query.getResultList());
    }
}
