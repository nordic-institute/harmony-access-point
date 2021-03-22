package eu.domibus.core.message.splitandjoin;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class MessageGroupDefaultService implements MessageGroupService {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageGroupDefaultService.class);

    @Autowired
    protected MessageGroupDao messageGroupDao;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void setSourceMessageId(String sourceMessageId, String groupId) {
        LOG.debug("Updating the SourceMessage id [{}] for group [{}]", sourceMessageId, groupId);
        final MessageGroupEntity messageGroupEntity = messageGroupDao.findByGroupId(groupId);
        messageGroupEntity.setSourceMessageId(sourceMessageId);
        messageGroupDao.update(messageGroupEntity);
    }
}
