package eu.domibus.core.ebms3.receiver.handler;

import eu.domibus.api.model.*;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Reliability;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.ebms3.mapper.Ebms3Converter;
import eu.domibus.core.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.core.ebms3.sender.ResponseHandler;
import eu.domibus.core.ebms3.sender.ResponseResult;
import eu.domibus.core.message.MessageExchangeConfiguration;
import eu.domibus.core.message.PartInfoDao;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.pull.IncomingPullReceiptHandler;
import eu.domibus.core.message.pull.MessagingLock;
import eu.domibus.core.message.pull.PullRequestResult;
import eu.domibus.core.message.reliability.ReliabilityChecker;
import eu.domibus.core.message.reliability.ReliabilityDTO;
import eu.domibus.core.message.reliability.ReliabilityService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.test.common.UserMessageSampleUtil;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Test;

import javax.xml.soap.SOAPMessage;

/**
 * @author François Gautier
 * @since 4.2
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "TestMethodWithIncorrectSignature"})
public class IncomingUserMessageReceiptHandlerTest {

    @Tested
    IncomingUserMessageReceiptHandler incomingUserMessageReceiptHandler;

    @Injectable
    ReliabilityService reliabilityService;
    @Injectable
    ReliabilityChecker reliabilityChecker;
    @Injectable
    UserMessageLogDao userMessageLogDao;
    @Injectable
    EbMS3MessageBuilder messageBuilder;
    @Injectable
    ResponseHandler responseHandler;
    @Injectable
    PModeProvider pModeProvider;
    @Injectable
    UserMessageDao userMessageDao;
    @Injectable
    MessageUtil messageUtil;
    @Injectable
    SoapUtil soapUtil;
    @Injectable
    Ebms3Converter ebms3Converter;
    @Injectable
    PartInfoDao partInfoDao;
    @Injectable
    IncomingPullReceiptHandler incomingPullReceiptHandler;

    @Test
    public void testHandleUserMessageReceipt_HappyFlow(@Injectable final SOAPMessage request,
                                                      @Injectable final SignalMessage signalMessage,
                                                      @Injectable final MessageExchangeConfiguration messageConfiguration,
                                                      @Injectable final PullRequestResult pullRequestResult,
                                                      @Injectable final MessagingLock messagingLock,
                                                      @Injectable final SOAPMessage soapMessage,
                                                      @Injectable final Reliability reliability,
                                                      @Injectable final LegConfiguration legConfiguration,
                                                      @Injectable ResponseResult responseResult) throws EbMS3Exception {
        final String messageId = "12345";
        final String pModeKey = "pmodeKey";
        UserMessage userMessage = UserMessageSampleUtil.createUserMessage();

        final UserMessageLog userMessageLog = new UserMessageLog();
        MessageStatusEntity messageStatusEntity = new MessageStatusEntity();
        messageStatusEntity.setMessageStatus(MessageStatus.WAITING_FOR_RECEIPT);
        userMessageLog.setMessageStatus(messageStatusEntity);
        userMessageLog.setUserMessage(userMessage);
        new Expectations(incomingUserMessageReceiptHandler) {{

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            result = messageConfiguration;

            messageConfiguration.getPmodeKey();
            result = pModeKey;

            responseHandler.verifyResponse(request, messageId);
            result = responseResult;

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            legConfiguration.getName();
            result = "NAME";

            incomingUserMessageReceiptHandler.getSourceMessageReliability();
            result = reliability;

            incomingUserMessageReceiptHandler.getSoapMessage(legConfiguration, userMessage);
            result = soapMessage;

            reliabilityChecker.check(soapMessage, request, responseResult, reliability);
            result = ReliabilityChecker.CheckResult.OK;

            userMessageDao.findByEntityId(userMessageLog.getEntityId());
            result = userMessage;
        }};

        incomingUserMessageReceiptHandler.handlePushUserMessageReceipt(request, messageId, userMessageLog);

        new FullVerifications() {{
            reliabilityService.handleReliability((ReliabilityDTO) any);
            times = 1;
        }};

    }

    @Test
    public void testHandleUserMessageReceipt_Exception(@Injectable final SOAPMessage request,
                                                      @Injectable final SignalMessage signalMessage,
                                                      @Injectable final MessageExchangeConfiguration messageConfiguration,
                                                      @Injectable final PullRequestResult pullRequestResult,
                                                      @Injectable final MessagingLock messagingLock,
                                                      @Injectable final SOAPMessage soapMessage,
                                                      @Injectable final Reliability reliability,
                                                      @Injectable final LegConfiguration legConfiguration,
                                                      @Injectable ResponseResult responseResult) throws EbMS3Exception {
        final String messageId = "12345";
        final String pModeKey = "pmodeKey";
        UserMessage userMessage = UserMessageSampleUtil.createUserMessage();
        final UserMessageLog userMessageLog = new UserMessageLog();
        MessageStatusEntity messageStatusEntity = new MessageStatusEntity();
        messageStatusEntity.setMessageStatus(MessageStatus.WAITING_FOR_RECEIPT);
        userMessageLog.setMessageStatus(messageStatusEntity);
        userMessageLog.setUserMessage(userMessage);

        new Expectations(incomingUserMessageReceiptHandler) {{
//            signalMessage.getRefToMessageId();
//            result = messageId;

            userMessageDao.findByEntityId(userMessageLog.getEntityId());
            result = userMessage;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            result = messageConfiguration;

            messageConfiguration.getPmodeKey();
            result = pModeKey;

            responseHandler.verifyResponse(request, messageId);
            result = EbMS3ExceptionBuilder
                    .getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0001)
                    .message("Mock Error")
                    .refToMessageId(messageId)
                    .build();

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            legConfiguration.getName();
            result = "NAME";

            incomingUserMessageReceiptHandler.getSoapMessage(legConfiguration, userMessage);
            result = soapMessage;

            reliabilityChecker.handleEbms3Exception((EbMS3Exception) any, userMessage);

        }};

        incomingUserMessageReceiptHandler.handlePushUserMessageReceipt(request, messageId, userMessageLog);

        new FullVerifications() {{
            reliabilityService.handleReliability((ReliabilityDTO) any);
            times = 1;
        }};

    }
}
