package eu.domibus.core.message.splitandjoin;

import eu.domibus.core.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.core.ebms3.sender.AbstractUserMessageSender;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPMessage;

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

    @Override
    protected void validateBeforeSending(UserMessage userMessage) {
        final String groupId = userMessage.getMessageFragment().getGroupId();
        final MessageGroupEntity groupEntity = messageGroupDao.findByGroupId(groupId);

        if (groupEntity.getExpired()) {
            throw new SplitAndJoinException("Group [" + groupId + "] is marked as expired");
        }

        if (groupEntity.getRejected()) {
            throw new SplitAndJoinException("Group [" + groupId + "] is marked as rejected");
        }
    }

    @Override
    protected SOAPMessage createSOAPMessage(UserMessage userMessage, LegConfiguration legConfiguration) throws EbMS3Exception {
        final MessageGroupEntity groupEntity = messageGroupDao.findByGroupId(userMessage.getMessageFragment().getGroupId());
        return messageBuilder.buildSOAPMessageForFragment(userMessage, groupEntity, legConfiguration);
    }

    @Override
    protected DomibusLogger getLog() {
        return LOG;
    }
}
