package eu.domibus.core.message.splitandjoin;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MSHRoleEntity;
import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.splitandjoin.MessageFragmentEntity;
import eu.domibus.api.model.splitandjoin.MessageGroupEntity;
import eu.domibus.core.message.UserMessageFactory;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.dictionary.MshRoleDao;
import eu.domibus.core.plugin.handler.MessageSubmitterImpl;
import eu.domibus.messaging.MessagingProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Ion Perpegel
 * @since 5.0
 * <p>
 * Spin-off from splitAndJoinService to break a cyclic dependency
 */
@Service
public class SplitAndJoinHelper {

    @Autowired
    protected UserMessageLogDao userMessageLogDao;

    @Autowired
    protected UserMessageFactory userMessageFactory;

    @Autowired
    MessageSubmitterImpl messageSubmitter;

    @Autowired
    protected MessageGroupDao messageGroupDao;

    @Autowired
    protected MshRoleDao mshRoleDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 1200) // 20 minutes
    public void createMessageFragments(UserMessage sourceMessage, MessageGroupEntity messageGroupEntity, List<String> fragmentFiles) {
        messageGroupDao.create(messageGroupEntity);

        String backendName = userMessageLogDao.findBackendForMessageId(sourceMessage.getMessageId(), sourceMessage.getMshRole().getRole());
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
        final MSHRoleEntity mshRoleEntity = mshRoleDao.findOrCreate(MSHRole.SENDING); // or the role of the source message
        userMessageFragment.setMshRole(mshRoleEntity);
        MessageFragmentEntity messageFragmentEntity = userMessageFactory.createMessageFragmentEntity(messageGroupEntity, fragmentNumber);
        PartInfo messageFragmentPartInfo = userMessageFactory.createMessageFragmentPartInfo(fragmentFile, fragmentNumber);
        messageSubmitter.submitMessageFragment(userMessageFragment, messageFragmentEntity, messageFragmentPartInfo, backendName);
    }
}
