package eu.domibus.core.message.splitandjoin;

import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.splitandjoin.MessageFragmentEntity;
import eu.domibus.api.model.splitandjoin.MessageGroupEntity;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Splitting;
import eu.domibus.core.message.MessageSubmitterService;
import eu.domibus.core.message.UserMessageFactory;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.messaging.MessagingProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Ion Perpegel
 * @since 5.0
 *
 * Spin-off from splitAndJoinService to break a cyclic dependency
 */
@Service
public class SplitAndJoinHelper {

    @Autowired
    protected UserMessageLogDao userMessageLogDao;

    @Autowired
    protected UserMessageFactory userMessageFactory;

    @Autowired
    MessageSubmitterService messageSubmitter;

    @Autowired
    protected MessageGroupDao messageGroupDao;

    public boolean mayUseSplitAndJoin(LegConfiguration legConfiguration) {
        final Splitting splitting = legConfiguration.getSplitting();
        if (splitting == null) {
            return false;
        }
        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 1200) // 20 minutes
    public void createMessageFragments(UserMessage sourceMessage, MessageGroupEntity messageGroupEntity, List<String> fragmentFiles) {
        messageGroupDao.create(messageGroupEntity);

        String backendName = userMessageLogDao.findBackendForMessageId(sourceMessage.getMessageId());
        for (int index = 0; index < fragmentFiles.size(); index++) {
            try {
                final String fragmentFile = fragmentFiles.get(index);
                createMessagingForFragment(sourceMessage, messageGroupEntity, backendName, fragmentFile, index + 1);
            } catch (MessagingProcessingException e) {
                throw new SplitAndJoinException("Could not create Messaging for fragment " + index, e);
            }
        }
    }

    protected void createMessagingForFragment(UserMessage sourceUserMessage, MessageGroupEntity messageGroupEntity, String backendName, String fragmentFile, int index) throws MessagingProcessingException {
        Long fragmentNumber = Long.valueOf(index);
        final UserMessage userMessageFragment = userMessageFactory.createUserMessageFragment(sourceUserMessage, messageGroupEntity, fragmentNumber, fragmentFile);
        MessageFragmentEntity messageFragmentEntity = userMessageFactory.createMessageFragmentEntity(messageGroupEntity, fragmentNumber);
        PartInfo messageFragmentPartInfo = userMessageFactory.createMessageFragmentPartInfo(fragmentFile, fragmentNumber);
        messageSubmitter.submitMessageFragment(userMessageFragment, messageFragmentEntity, messageFragmentPartInfo, backendName);
    }
}
