package eu.domibus.core.message;

import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.splitandjoin.MessageFragmentEntity;
import eu.domibus.api.model.splitandjoin.MessageGroupEntity;
import eu.domibus.core.dao.BasicDao;
import org.springframework.stereotype.Repository;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Repository
public class MessageFragmentDao extends BasicDao<MessageFragmentEntity> {

    public MessageFragmentDao() {
        super(MessageFragmentEntity.class);
    }

    @Override
    public void create(MessageFragmentEntity messageFragmentEntity) {
        if (!em.contains(messageFragmentEntity.getUserMessage())) {
            UserMessage attachedUserMessage = em.find(UserMessage.class, messageFragmentEntity.getUserMessage().getEntityId());
            messageFragmentEntity.setUserMessage(attachedUserMessage);
        }
        super.create(messageFragmentEntity);
    }
}
