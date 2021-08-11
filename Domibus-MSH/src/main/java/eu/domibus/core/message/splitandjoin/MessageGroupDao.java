package eu.domibus.core.message.splitandjoin;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.splitandjoin.MessageGroupEntity;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Repository
public class MessageGroupDao extends BasicDao<MessageGroupEntity> {


    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageGroupDao.class);

    public MessageGroupDao() {
        super(MessageGroupEntity.class);
    }

    public MessageGroupEntity findByUserMessageEntityIdWithMessageHeader(Long userMessageEntityId) {
        final TypedQuery<MessageGroupEntity> namedQuery = em.createNamedQuery("MessageGroupEntity.findByUserMessageEntityIdWithMessageHeader", MessageGroupEntity.class);
        namedQuery.setParameter("USER_MESSAGE_ENTITY_ID", userMessageEntityId);
        return DataAccessUtils.singleResult(namedQuery.getResultList());
    }

    public MessageGroupEntity findByUserMessageEntityId(Long userMessageEntityId) {
        final TypedQuery<MessageGroupEntity> namedQuery = em.createNamedQuery("MessageGroupEntity.findByUserMessageEntityId", MessageGroupEntity.class);
        namedQuery.setParameter("USER_MESSAGE_ENTITY_ID", userMessageEntityId);
        return DataAccessUtils.singleResult(namedQuery.getResultList());
    }

    public MessageGroupEntity findByGroupId(String groupId) {
        final TypedQuery<MessageGroupEntity> namedQuery = em.createNamedQuery("MessageGroupEntity.findByGroupId", MessageGroupEntity.class);
        namedQuery.setParameter("GROUP_ID", groupId);
        return DataAccessUtils.singleResult(namedQuery.getResultList());
    }

    @Transactional
    public MessageGroupEntity findByGroupIdWithMessageHeader(String groupId) {
        final TypedQuery<MessageGroupEntity> namedQuery = em.createNamedQuery("MessageGroupEntity.findByGroupIdWithMessageHeader", MessageGroupEntity.class);
        namedQuery.setParameter("GROUP_ID", groupId);
        return DataAccessUtils.singleResult(namedQuery.getResultList());
    }

    public List<MessageGroupEntity> findOngoingReceivedNonExpiredOrRejected() {
        TypedQuery<MessageGroupEntity> query = this.em.createNamedQuery("MessageGroupEntity.findReceivedNonExpiredOrRejected", MessageGroupEntity.class);
        query.setParameter("MSH_ROLE", MSHRole.RECEIVING);
        return query.getResultList();
    }

    public List<MessageGroupEntity> findOngoingSendNonExpiredOrRejected() {
        TypedQuery<MessageGroupEntity> query = this.em.createNamedQuery("MessageGroupEntity.findSendNonExpiredOrRejected", MessageGroupEntity.class);
        query.setParameter("MSH_ROLE", MSHRole.SENDING);
        query.setParameter("SOURCE_MSG_STATUS", MessageStatus.SEND_ENQUEUED);
        return query.getResultList();
    }

    @Override
    public void create(MessageGroupEntity messageGroupEntity) {
        if(messageGroupEntity.getSourceMessage() == null) {
            UserMessage um = new UserMessage();
            um.setEntityId(UserMessageDefaultService.DEFAULT_USER_MESSAGE_ID_PK);
            messageGroupEntity.setSourceMessage(um);
        }
        super.create(messageGroupEntity);
    }
}
