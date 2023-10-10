package eu.domibus.core.message;

import eu.domibus.api.model.AbstractBaseEntity;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.MessageStatusEntity;
import eu.domibus.core.dao.SingleValueDictionaryDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Service;

import javax.persistence.TypedQuery;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author Federico Martini
 * @author Cosmin Baciu
 * @implNote This DAO class works with {@link MessageStatusEntity}, which is a static dictionary
 * based on the {@link MessageStatus} enum: no new values are expected to be added at runtime;
 * therefore, {@code MessageStatusDao} can be used directly, without subclassing {@code AbstractDictionaryService}.
 * @since 3.2
 */
@Service
public class MessageStatusDao extends SingleValueDictionaryDao<MessageStatusEntity> {

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
        return getEntity(messageStatus);
    }

    @Override
    public MessageStatusEntity findByValue(final Object messageStatus) {
        return getEntity(messageStatus);
    }

    public List<Long> getEntityIdsOf(List<MessageStatus> statuses) {
        return statuses.stream()
                .map(this::findByValue)
                .map(AbstractBaseEntity::getEntityId)
                .collect(Collectors.toList());
    }

    private MessageStatusEntity getEntity(Object messageStatus) {
        TypedQuery<MessageStatusEntity> query = em.createNamedQuery("MessageStatusEntity.findByStatus", MessageStatusEntity.class);
        query.setParameter("MESSAGE_STATUS", messageStatus);
        return DataAccessUtils.singleResult(query.getResultList());
    }
}
