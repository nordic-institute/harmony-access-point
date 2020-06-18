package eu.domibus.core.message.splitandjoin;

import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.core.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.core.ebms3.sender.ResponseHandler;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.core.ebms3.ws.policy.PolicyService;
import eu.domibus.core.error.ErrorLogDao;
import eu.domibus.core.message.MessageExchangeService;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.reliability.ReliabilityChecker;
import eu.domibus.core.message.reliability.ReliabilityService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.ebms3.common.model.UserMessage;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@RunWith(JMockit.class)
public class MessageFragmentSenderTest {

    @Tested
    MessageFragmentSender messageFragmentSender;

    @Injectable
    protected PModeProvider pModeProvider;

    @Injectable
    protected MSHDispatcher mshDispatcher;

    @Injectable
    protected EbMS3MessageBuilder messageBuilder;

    @Injectable
    protected ReliabilityChecker reliabilityChecker;

    @Injectable
    protected ResponseHandler responseHandler;

    @Injectable
    protected MessageAttemptService messageAttemptService;

    @Injectable
    protected MessageExchangeService messageExchangeService;

    @Injectable
    protected PolicyService policyService;

    @Injectable
    protected ReliabilityService reliabilityService;

    @Injectable
    protected UserMessageLogDao userMessageLogDao;

    @Injectable
    protected MessageGroupDao messageGroupDao;

    @Injectable
    protected ErrorLogDao errorLogDao;


    @Test
    public void validateBeforeSendingSuccessful(@Injectable UserMessage userMessage,
                                      @Injectable MessageGroupEntity groupEntity) {
        String groupId = "123";

        new Expectations() {{
            userMessage.getMessageFragment().getGroupId();
            result = groupId;

            messageGroupDao.findByGroupId(groupId);
            result = groupEntity;
        }};

        messageFragmentSender.validateBeforeSending(userMessage);
    }

    @Test(expected = SplitAndJoinException.class)
    public void validateBeforeSendingExpiredGroup(@Injectable UserMessage userMessage,
                                                @Injectable MessageGroupEntity groupEntity) {
        String groupId = "123";

        new Expectations() {{
            userMessage.getMessageFragment().getGroupId();
            result = groupId;

            messageGroupDao.findByGroupId(groupId);
            result = groupEntity;

            groupEntity.getExpired();
            result = true;
        }};

        messageFragmentSender.validateBeforeSending(userMessage);
    }

    @Test(expected = SplitAndJoinException.class)
    public void validateBeforeSendingRejectedGroup(@Injectable UserMessage userMessage,
                                                  @Injectable MessageGroupEntity groupEntity) {
        String groupId = "123";

        new Expectations() {{
            userMessage.getMessageFragment().getGroupId();
            result = groupId;

            messageGroupDao.findByGroupId(groupId);
            result = groupEntity;

            groupEntity.getExpired();
            result = false;

            groupEntity.getRejected();
            result = true;
        }};

        messageFragmentSender.validateBeforeSending(userMessage);
    }
}