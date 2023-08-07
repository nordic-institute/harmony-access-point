package eu.domibus.core.message;

import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.MessageStatusEntity;
import eu.domibus.core.dao.BasicDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Service;

import javax.persistence.TypedQuery;


/**
 * @author Federico Martini
 * @author Cosmin Baciu
 * @since 3.2
 *
 * @implNote This DAO class works with {@link MessageStatusEntity}, which is a static dictionary
 * based on the {@link MessageStatus} enum: no new values are expected to be added at runtime;
 * therefore, {@link MessageStatusDao} can be used directly, without subclassing AbstractDictionaryService.
 */
@Service
public class MessageStatusDao extends BasicDao<MessageStatusEntity> {

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
