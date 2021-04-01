package eu.domibus.core.message.splitandjoin;

import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.splitandjoin.MessageFragmentEntity;
import eu.domibus.api.model.splitandjoin.MessageGroupEntity;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.api.model.UserMessage;
import eu.domibus.core.ebms3.sender.AbstractUserMessageSender;
import eu.domibus.core.message.MessageFragmentDao;
import eu.domibus.core.message.PartInfoDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPMessage;
import java.util.List;

/**
 * Class responsible for sending AS4 MessageFragments to C3
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class MessageFragmentSender extends AbstractUserMessageSender {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageFragmentSender.class);

    @Autowired
    protected MessageGroupDao messageGroupDao;

    @Autowired
    protected MessageFragmentDao messageFragmentDao;

    @Autowired
    protected PartInfoDao partInfoDao;

    @Override
    protected void validateBeforeSending(UserMessage userMessage) {
        final MessageGroupEntity groupEntity = messageGroupDao.findByUserMessageEntityId(userMessage.getEntityId());
        String groupId = groupEntity.getGroupId();

        if (groupEntity.getExpired()) {
            throw new SplitAndJoinException("Group [" + groupId + "] is marked as expired");
        }

        if (groupEntity.getRejected()) {
            throw new SplitAndJoinException("Group [" + groupId + "] is marked as rejected");
        }
    }

    @Override
    protected SOAPMessage createSOAPMessage(UserMessage userMessage, LegConfiguration legConfiguration) throws EbMS3Exception {
        final MessageGroupEntity groupEntity = messageGroupDao.findByUserMessageEntityId(userMessage.getEntityId());
        MessageFragmentEntity messageFragmentEntity = messageFragmentDao.read(userMessage.getEntityId());
        List<PartInfo> partInfos = partInfoDao.findPartInfoByUserMessageEntityId(userMessage.getEntityId());
        return messageBuilder.buildSOAPMessageForFragment(userMessage, messageFragmentEntity, partInfos, groupEntity, legConfiguration);
    }

    @Override
    protected DomibusLogger getLog() {
        return LOG;
    }
}
