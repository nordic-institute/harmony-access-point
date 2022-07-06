package eu.domibus.core.message.splitandjoin;

import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.splitandjoin.MessageGroupEntity;
import eu.domibus.core.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.core.ebms3.sender.MessageSenderService;
import eu.domibus.core.ebms3.sender.ResponseHandler;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.core.ebms3.ws.policy.PolicyService;
import eu.domibus.core.error.ErrorLogDao;
import eu.domibus.core.error.ErrorLogService;
import eu.domibus.core.message.*;
import eu.domibus.core.message.dictionary.MshRoleDao;
import eu.domibus.core.message.nonrepudiation.NonRepudiationService;
import eu.domibus.core.message.reliability.ReliabilityChecker;
import eu.domibus.core.message.reliability.ReliabilityService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.util.SoapUtil;
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
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class MessageFragmentSenderTest {

    public static final Long ENTITY_ID = 1L;
    @Tested
    MessageFragmentSender messageFragmentSender;

    @Injectable
    protected PModeProvider pModeProvider;

    @Injectable
    protected SoapUtil soapUtil;

    @Injectable
    protected ErrorLogService errorLogService;

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

    @Injectable
    protected NonRepudiationService nonRepudiationService;

    @Injectable
    protected MessageFragmentDao messageFragmentDao;

    @Injectable
    protected PartInfoDao partInfoDao;

    @Injectable
    public MshRoleDao mshRoleDao;

    @Injectable
    UserMessage userMessage;

    @Injectable
    MessageGroupEntity groupEntity;

    @Injectable
    protected UserMessageServiceHelper userMessageServiceHelper;

    @Injectable
    MessageSenderService messageSenderService;

    @Test
    public void validateBeforeSendingSuccessful() {
        new Expectations() {{
            userMessage.getEntityId();
            result = ENTITY_ID;

            messageGroupDao.findByUserMessageEntityId(ENTITY_ID);
            result = groupEntity;
        }};

        messageFragmentSender.validateBeforeSending(userMessage);
    }

    @Test(expected = SplitAndJoinException.class)
    public void validateBeforeSendingExpiredGroup() {
        new Expectations() {{
            userMessage.getEntityId();
            result = ENTITY_ID;

            messageGroupDao.findByUserMessageEntityId(ENTITY_ID);
            result = groupEntity;

            groupEntity.getExpired();
            result = true;
        }};

        messageFragmentSender.validateBeforeSending(userMessage);
    }

    @Test(expected = SplitAndJoinException.class)
    public void validateBeforeSendingRejectedGroup(   ) {
        new Expectations() {{
            userMessage.getEntityId();
            result = ENTITY_ID;

            messageGroupDao.findByUserMessageEntityId(ENTITY_ID);
            result = groupEntity;

            groupEntity.getExpired();
            result = false;

            groupEntity.getRejected();
            result = true;
        }};

        messageFragmentSender.validateBeforeSending(userMessage);
    }
}
