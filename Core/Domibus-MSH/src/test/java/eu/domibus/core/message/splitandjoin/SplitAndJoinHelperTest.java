package eu.domibus.core.message.splitandjoin;

import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.splitandjoin.MessageGroupEntity;
import eu.domibus.core.message.UserMessageFactory;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.plugin.handler.MessageSubmitterImpl;
import eu.domibus.messaging.MessagingProcessingException;
import junit.framework.TestCase;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(JMockit.class)
public class SplitAndJoinHelperTest extends TestCase {

    @Injectable
    protected UserMessageLogDao userMessageLogDao;

    @Injectable
    protected UserMessageFactory userMessageFactory;

    @Injectable
    MessageSubmitterImpl messageSubmitter;

    @Injectable
    protected MessageGroupDao messageGroupDao;

    @Tested
    SplitAndJoinHelper splitAndJoinHelper;

    @Test
    public void createMessagingForFragment(@Injectable UserMessage sourceMessage,
                                           @Injectable MessageGroupEntity messageGroupEntity,
                                           @Injectable UserMessage userMessageFragment) throws MessagingProcessingException {
        String backendName = "mybackend";

        final String fragment1 = "fragment1";

        new Expectations() {{
            userMessageFactory.createUserMessageFragment(sourceMessage, messageGroupEntity, 1L, fragment1);
            result = userMessageFragment;
        }};

        splitAndJoinHelper.createMessagingForFragment(sourceMessage, messageGroupEntity, backendName, fragment1, 1);

        new Verifications() {{
            userMessageFactory.createUserMessageFragment(sourceMessage, messageGroupEntity, 1L, fragment1);
        }};
    }

    @Test
    public void createMessageFragments(@Injectable UserMessage sourceMessage,
                                       @Injectable MessageGroupEntity messageGroupEntity
    ) throws MessagingProcessingException {
        String messageId = "123";
        String backendName = "mybackend";

        List<String> fragmentFiles = new ArrayList<>();
        final String fragment1 = "fragment1";
        fragmentFiles.add(fragment1);


        new Expectations(splitAndJoinHelper) {{
            sourceMessage.getMessageId();
            result = messageId;

            userMessageLogDao.findBackendForMessageId(messageId, sourceMessage.getMshRole().getRole());
            result = backendName;

            splitAndJoinHelper.createMessagingForFragment(sourceMessage, messageGroupEntity, backendName, fragment1, 1);
            times = 1;
        }};

        splitAndJoinHelper.createMessageFragments(sourceMessage, messageGroupEntity, fragmentFiles);

        new Verifications() {{
            messageGroupDao.create(messageGroupEntity);


        }};
    }

}
