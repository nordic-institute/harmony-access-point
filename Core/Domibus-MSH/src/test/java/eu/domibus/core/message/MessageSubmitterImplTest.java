package eu.domibus.core.message;

import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.message.UserMessageSecurityService;
import eu.domibus.api.message.validation.UserMessageValidatorSpiService;
import eu.domibus.api.model.*;
import eu.domibus.api.model.splitandjoin.MessageFragmentEntity;
import eu.domibus.api.payload.PartInfoService;
import eu.domibus.api.pmode.PModeConstants;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.*;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.error.ErrorLogService;
import eu.domibus.core.generator.id.MessageIdGenerator;
import eu.domibus.core.message.compression.CompressionException;
import eu.domibus.core.message.compression.CompressionService;
import eu.domibus.core.message.dictionary.MpcDictionaryService;
import eu.domibus.core.message.dictionary.MshRoleDao;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.message.splitandjoin.SplitAndJoinHelper;
import eu.domibus.core.message.splitandjoin.SplitAndJoinService;
import eu.domibus.core.payload.PayloadProfileValidator;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.core.plugin.handler.BackendMessageValidator;
import eu.domibus.core.plugin.handler.DatabaseMessageHandlerTest;
import eu.domibus.core.plugin.handler.MessageSubmitterImpl;
import eu.domibus.core.plugin.transformer.SubmissionAS4Transformer;
import eu.domibus.core.pmode.PModeDefaultService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.pmode.validation.validators.MessagePropertyValidator;
import eu.domibus.core.pmode.validation.validators.PropertyProfileValidator;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.DuplicateMessageException;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.messaging.PModeMismatchException;
import eu.domibus.plugin.ProcessingType;
import eu.domibus.plugin.Submission;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import javax.jms.Queue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import static eu.domibus.test.common.UserMessageSampleUtil.createUserMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
@RunWith(JMockit.class)
public class MessageSubmitterImplTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DatabaseMessageHandlerTest.class);
    private static final String BACKEND = "backend";

    private static final String MESS_ID = UUID.randomUUID().toString();

    private static final String GREEN = "green_gw";
    private static final String RED = "red_gw";
    private static final String BLUE = "blue_gw";
    private static final String AGREEMENT = "";
    private static final String SERVICE = "testService1";
    private static final String ACTION = "TC2Leg1";
    private static final String LEG = "pushTestcase1tc2Action";

    private final String pModeKey = GREEN + PModeConstants.PMODEKEY_SEPARATOR +
            RED + PModeConstants.PMODEKEY_SEPARATOR +
            SERVICE + PModeConstants.PMODEKEY_SEPARATOR +
            ACTION + PModeConstants.PMODEKEY_SEPARATOR +
            AGREEMENT + PModeConstants.PMODEKEY_SEPARATOR +
            LEG;

    @Tested
    private MessageSubmitterImpl messageSubmitterImpl;

    @Injectable
    private PModeDefaultService pModeDefaultService;

    @Injectable
    private UserMessageDefaultService userMessageDefaultService;

    @Injectable
    private UserMessageServiceHelper userMessageServiceHelper;

    @Injectable
    private JMSManager jmsManager;

    @Injectable
    private AuthUtils authUtils;

    @Injectable
    private LegConfiguration legConfiguration;

    @Injectable
    private SplitAndJoinService splitAndJoinService;

    @Injectable
    private PayloadFileStorageProvider storageProvider;

    @Injectable
    private MessagePropertyValidator messagePropertyValidator;

    @Injectable
    private Queue sendMessageQueue;

    @Injectable
    private MessageExchangeService messageExchangeService;

    @Injectable
    private CompressionService compressionService;

    @Injectable
    private SubmissionAS4Transformer transformer;

    @Injectable
    private MessagingService messagingService;

    @Injectable
    private SignalMessageDao signalMessageDao;

    @Injectable
    private UserMessageLogDefaultService userMessageLogService;

    @Injectable
    private SignalMessageLogDao signalMessageLogDao;

    @Injectable
    private ErrorLogService errorLogService;

    @Injectable
    private PModeProvider pModeProvider;

    @Injectable
    private MessageIdGenerator messageIdGenerator;

    @Injectable
    private PayloadProfileValidator payloadProfileValidator;

    @Injectable
    private PropertyProfileValidator propertyProfileValidator;

    @Injectable
    private BackendMessageValidator backendMessageValidator;

    @Injectable
    private PullMessageService pullMessageService;

    @Injectable
    private UserMessageDefaultService userMessageService;

    @Injectable
    private UserMessageDao userMessageDao;

    @Injectable
    private PartInfoDao partInfoDao;

    @Injectable
    private MessageFragmentDao messageFragmentDao;

    @Injectable
    private MpcDictionaryService mpcDictionaryService;

    @Injectable
    private MshRoleDao mshRoleDao;

    @Injectable
    private PartInfoService partInfoService;

    @Injectable
    UserMessageHandlerServiceImpl userMessageHandlerService;

    @Injectable
    UserMessageValidatorSpiService userMessageValidatorSpiService;

    @Injectable
    protected UserMessageSecurityService userMessageSecurityService;

    @Injectable
    protected ApplicationEventPublisher applicationEventPublisher;

    @Injectable
    protected SplitAndJoinHelper splitAndJoinHelper;

    @Test
    public void testSubmitPullMessagePModeNOk(@Injectable final Submission messageData) throws Exception {
        new Expectations() {{
            messageData.getProcessingType();
            result = ProcessingType.PULL;
            UserMessage userMessage = createUserMessage();
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, false, ProcessingType.PULL);
            MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration("", "green_gw", "red_gw", "testService1", "TC2Leg1", "pushTestcase1tc2Action");
            result = messageExchangeConfiguration;

            messageExchangeService.getMessageStatus(messageExchangeConfiguration, ProcessingType.PULL);
            result = new PModeException(DomibusCoreErrorCode.DOM_003, "invalid pullprocess configuration");
        }};

        try {
            messageSubmitterImpl.submit(messageData, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (PModeMismatchException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(ErrorCode.EBMS_0010, mpEx.getEbms3ErrorCode());
            assertTrue(mpEx.getMessage().contains("invalid pullprocess configuration"));
        }

        new Verifications() {{
            authUtils.getOriginalUserWithUnsecureLoginAllowed();
            messageIdGenerator.generateMessageId();
            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING, false, ProcessingType.PULL);
        }};
    }

    @Test
    public void testSubmitMessageWithRefIdNOk(@Injectable final Submission submission) throws Exception {
        String refToMessageId = "abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656@domibus.eu";

        new Expectations() {{
            submission.getMessageId();
            result = "messageId";

            backendMessageValidator.validateSubmissionSending(submission);
            result = EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0008)
                    .message("RefToMessageId value is too long (over 255 characters)")
                    .refToMessageId(refToMessageId)
                    .build();
        }};

        try {
            messageSubmitterImpl.submit(submission, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (MessagingProcessingException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(ErrorCode.EBMS_0008, mpEx.getEbms3ErrorCode());
        }

        new FullVerifications() {{
            authUtils.getOriginalUserWithUnsecureLoginAllowed();
            times = 1;
            authUtils.isUnsecureLoginAllowed();
            times = 1;
            authUtils.hasUserOrAdminRole();
            times = 1;
            errorLogService.createErrorLog((EbMS3Exception) any, MSHRole.SENDING, null);
            times = 1;
        }};

    }

    @Test
    public void testSubmitMessageStoreNOk(@Injectable final Submission messageData, @Injectable PartInfo partInfo) throws Exception {
        new Expectations() {{

            messageData.getProcessingType();
            result = ProcessingType.PUSH;

            authUtils.getOriginalUserWithUnsecureLoginAllowed();
            result = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";

            UserMessage userMessage = createUserMessage();
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, false, ProcessingType.PUSH);
            result = new MessageExchangeConfiguration("", "green_gw", "red_gw", "testService1", "TC2Leg1", "pushTestcase1tc2Action");

            Party sender = new Party();
            sender.setName(GREEN);
            pModeProvider.getSenderParty(pModeKey);
            result = sender;

            Party receiver = new Party();
            receiver.setName(RED);
            pModeProvider.getReceiverParty(pModeKey);
            result = receiver;

            Party confParty = new Party();
            confParty.setName(GREEN);

            pModeProvider.getGatewayParty();
            result = confParty;

            LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);

            messagingService.storeMessagePayloads(new UserMessage(), null, MSHRole.SENDING, legConfiguration, anyString);
            result = new CompressionException("Could not store binary data for message due to IO exception", new IOException("test compression"));
        }};

        try {
            messageSubmitterImpl.submit(messageData, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (MessagingProcessingException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(ErrorCode.EBMS_0303, mpEx.getEbms3ErrorCode());
            assertTrue(mpEx.getMessage().contains("Could not store binary data for message due to IO exception"));
        }

        new Verifications() {{
            authUtils.getOriginalUserWithUnsecureLoginAllowed();
            messageIdGenerator.generateMessageId();
            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING, false, ProcessingType.PUSH);
            pModeProvider.getLegConfiguration(anyString);
            messagingService.storeMessagePayloads(withAny(new UserMessage()), null, MSHRole.SENDING, legConfiguration, anyString);
        }};
    }

    @Test
    /* Tests a submit message where from and to parties are the same. */
    public void testSubmitMessageBlue2BlueNOk(@Injectable final Submission submission,
                                              @Injectable final MessageExchangeConfiguration userMessageExchangeConfiguration,
                                              @Injectable final Party from,
                                              @Injectable final Party to) throws Exception {
        UserMessage userMessage = createUserMessage();
        new Expectations() {{

            submission.getProcessingType();
            result = ProcessingType.PUSH;

            submission.getMessageId();
            result = "messageId";

            submission.getProcessingType();
            result = ProcessingType.PUSH;


            transformer.transformFromSubmission(submission);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            transformer.generatePartInfoList(submission);
            result = new ArrayList<>();

            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING, false, ProcessingType.PUSH);
            result = userMessageExchangeConfiguration;
            times = 1;

            userMessageExchangeConfiguration.getPmodeKey();
            result = "pmodeKey";

            pModeProvider.getSenderParty("pmodeKey");
            result = from;

            pModeProvider.getReceiverParty("pmodeKey");
            result = to;

            backendMessageValidator.validateParties(from, to);
            result = EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0010)
                    .message("The initiator party's name is the same as the responder party's one")
                    .build();
        }};

        try {
            messageSubmitterImpl.submit(submission, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (MessagingProcessingException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(ErrorCode.EBMS_0010, mpEx.getEbms3ErrorCode());
            assertTrue(mpEx.getMessage().contains("The initiator party's name is the same as the responder party's one"));
        }

        new Verifications() {{
            authUtils.getOriginalUserWithUnsecureLoginAllowed();
            times = 1;
            authUtils.isUnsecureLoginAllowed();
            times = 1;
            authUtils.hasUserOrAdminRole();
            times = 1;
            messageIdGenerator.generateMessageId();
            times = 1;
            backendMessageValidator.validateSubmissionSending(submission);
            times = 1;
            errorLogService.createErrorLog((EbMS3Exception) any, MSHRole.SENDING, null);
            times = 1;
        }};
    }

    @Test
    public void testVerifyOriginalUserNOk(@Injectable final Submission messageData,
                                          @Injectable UserMessage userMessage) throws Exception {

        String originalUser = "mycorner";
        new Expectations(messageSubmitterImpl) {{
            authUtils.getOriginalUserWithUnsecureLoginAllowed();
            result = originalUser;

            transformer.transformFromSubmission(messageData);
            result = userMessage;

            userMessageSecurityService.validateUserAccessWithUnsecureLoginAllowed(userMessage, originalUser, MessageConstants.ORIGINAL_SENDER);
            result = new AccessDeniedException("You are not allowed to handle this message. You are authorized as [" + originalUser + "]");
        }};

        try {
            messageSubmitterImpl.submit(messageData, BACKEND);
            Assert.fail("It should throw AccessDeniedException");
        } catch (AccessDeniedException ex) {
            LOG.debug("AccessDeniedException catched: " + ex.getMessage());
            assertTrue(ex.getMessage().contains("You are not allowed to handle this message. You are authorized as [mycorner]"));
        }

        new Verifications() {{
            authUtils.getOriginalUserWithUnsecureLoginAllowed();
        }};
    }

    @Test
    public void submitMessageFragmentTest(@Injectable UserMessage userMessage,
                                          @Mocked Messaging message,
                                          @Injectable MessageStatusEntity messageStatus,
                                          @Injectable MSHRole mshRole,
                                          @Injectable ObjectFactory ebMS3Of,
                                          @Injectable MessageExchangeConfiguration userMessageExchangeConfiguration,
                                          @Injectable Party to,
                                          @Injectable LegConfiguration legConfiguration) throws EbMS3Exception, MessagingProcessingException {
        String backendName = "backendName";
        String messageId = UUID.randomUUID().toString();
        String pModeKey = "pmodeKey";

        new Expectations(messageSubmitterImpl) {{
            userMessage.getMessageId();
            result = messageId;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            result = userMessageExchangeConfiguration;

            userMessageExchangeConfiguration.getPmodeKey();
            result = pModeKey;

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            messageExchangeService.getMessageStatusForPush();
            result = messageStatus;

            messageStatus.getMessageStatus();
            result = MessageStatus.SEND_ENQUEUED;
        }};

        messageSubmitterImpl.submitMessageFragment(userMessage, new MessageFragmentEntity(), null, backendName);

    }

    @Test
    public void testSubmitMessageGreen2RedOk(@Injectable final Submission messageData,
                                             @Injectable PartInfo partInfo,
                                             @Injectable MessageExchangeConfiguration messageExchangeConfiguration,
                                             @Injectable Party sender,
                                             @Injectable Party receiver,
                                             @Injectable Party confParty,
                                             @Injectable MessageStatusEntity messageStatus) throws Exception {
        final UserMessage userMessage = new UserMessage();
        new Expectations() {{
            authUtils.getOriginalUserWithUnsecureLoginAllowed();
            result = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";

            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            messageData.getProcessingType();
            result = ProcessingType.PUSH;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, false, messageData.getProcessingType());
            result = messageExchangeConfiguration;

            messageExchangeConfiguration.getPmodeKey();
            result = pModeKey;

            pModeProvider.getSenderParty(pModeKey);
            result = sender;

            pModeProvider.getReceiverParty(pModeKey);
            result = receiver;

            pModeProvider.getGatewayParty();
            result = confParty;
            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;
        }};

        final String messageId = messageSubmitterImpl.submit(messageData, BACKEND);
        assertEquals(MESS_ID, messageId);

        new Verifications() {{
            authUtils.getOriginalUserWithUnsecureLoginAllowed();
            messageIdGenerator.generateMessageId();
            pModeProvider.getLegConfiguration(pModeKey);
            messagingService.storeMessagePayloads(withAny(new UserMessage()), null, MSHRole.SENDING, withAny(new LegConfiguration()), anyString);
        }};
    }

    @Test
    public void testSubmitMessageGreen2RedNOk(@Injectable final Submission submission,
                                              @Injectable final Party gatewayParty,
                                              @Injectable final Party from,
                                              @Injectable final Party to) throws Exception {
        UserMessage userMessage = createUserMessage();
        new Expectations() {{
            submission.getMessageId();
            result = "messageId";

            submission.getProcessingType();
            result = ProcessingType.PUSH;


            transformer.transformFromSubmission(submission);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, false, ProcessingType.PUSH);
            result = new MessageExchangeConfiguration("", "green_gw", "red_gw", "testService1", "TC2Leg1", "pushTestcase1tc2Action");

            // Here the configuration of the access point is supposed to be BLUE!
            Party confParty = new Party();
            confParty.setName(BLUE);

            pModeProvider.getGatewayParty();
            result = confParty;

            transformer.generatePartInfoList(submission);
            result = new ArrayList<>();

            pModeProvider.getSenderParty(pModeKey);
            result = from;
            pModeProvider.getReceiverParty(pModeKey);
            result = to;
            backendMessageValidator.validateParties(from, to);

            pModeProvider.getGatewayParty();
            result = gatewayParty;

            backendMessageValidator.validateInitiatorParty(gatewayParty, from);
            result = EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0010)
                    .message("The initiator party's name [" + GREEN + "] does not correspond to the access point's name [" + BLUE + "]")
                    .build();

        }};

        try {
            messageSubmitterImpl.submit(submission, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (MessagingProcessingException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(ErrorCode.EBMS_0010, mpEx.getEbms3ErrorCode());
            assertTrue(mpEx.getMessage().contains("does not correspond to the access point's name"));
        }

        new Verifications() {{
            authUtils.getOriginalUserWithUnsecureLoginAllowed();
            times = 1;
            authUtils.isUnsecureLoginAllowed();
            times = 1;
            authUtils.hasUserOrAdminRole();
            times = 1;
            messageIdGenerator.generateMessageId();
            times = 1;

            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING, false, ProcessingType.PUSH);
            times = 1;
            backendMessageValidator.validateParties(withAny(new Party()), withAny(new Party()));
            times = 1;
            backendMessageValidator.validateInitiatorParty(withAny(new Party()), withAny(new Party()));
            times = 1;
            backendMessageValidator.validateSubmissionSending(submission);
            times = 1;
            errorLogService.createErrorLog((EbMS3Exception) any, MSHRole.SENDING, null);
            times = 1;
        }};

    }

    @Test
    public void testSubmitDuplicateMessage(@Injectable final Submission submission, @Injectable UserMessage userMessage) throws Exception {
        new Expectations() {{
            backendMessageValidator.validateSubmissionSending(submission);
            result = new DuplicateMessageException("Message with id [" + MESS_ID + "] already exists. Message identifiers must be unique");
        }};

        try {
            messageSubmitterImpl.submit(submission, BACKEND);
            Assert.fail("It should throw " + DuplicateMessageException.class.getCanonicalName());
        } catch (DuplicateMessageException ex) {
            LOG.debug("DuplicateMessageException catched: " + ex.getMessage());
            assertTrue(ex.getMessage().contains("already exists. Message identifiers must be unique"));
        }

        new Verifications() {{
            authUtils.getOriginalUserWithUnsecureLoginAllowed();
        }};
    }

    @Test
    public void testSubmitMessagePModeNOk(@Injectable final Submission submission) throws Exception {
        UserMessage userMessage = createUserMessage();

        new Expectations() {{
            submission.getMessageId();
            result = "messageId";

            submission.getProcessingType();
            result = ProcessingType.PUSH;

            transformer.transformFromSubmission(submission);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            transformer.generatePartInfoList(submission);
            result = new ArrayList<>();

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, false, ProcessingType.PUSH);
            result = EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0010)
                    .message("PMode could not be found. Are PModes configured in the database?")
                    .refToMessageId(MESS_ID)
                    .build();
        }};

        try {
            messageSubmitterImpl.submit(submission, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (MessagingProcessingException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(ErrorCode.EBMS_0010, mpEx.getEbms3ErrorCode());
            assertTrue(mpEx.getMessage().contains("PMode could not be found. Are PModes configured in the database?"));
        }

        new Verifications() {{
            authUtils.getOriginalUserWithUnsecureLoginAllowed();
            times = 1;
            authUtils.isUnsecureLoginAllowed();
            times = 1;
            authUtils.hasUserOrAdminRole();
            times = 1;
            messageIdGenerator.generateMessageId();
            times = 1;
            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING, false, ProcessingType.PUSH);
            times = 1;
            backendMessageValidator.validateSubmissionSending(submission);
            times = 1;
            errorLogService.createErrorLog((EbMS3Exception) any, MSHRole.SENDING, null);
            times = 1;

        }};
    }

    @Test
    public void testSubmitPullMessageGreen2RedOk(@Injectable final Submission messageData, @Injectable PartInfo partInfo,
                                                 @Injectable MessageStatusEntity messageStatus) throws Exception {
        new Expectations() {{

            authUtils.getOriginalUserWithUnsecureLoginAllowed();
            result = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";

            UserMessage userMessage = createUserMessage();
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            messageData.getProcessingType();
            result = ProcessingType.PULL;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, false, messageData.getProcessingType());
            result = new MessageExchangeConfiguration("", "green_gw", "red_gw", "testService1", "TC2Leg1", "pushTestcase1tc2Action");

            Party sender = new Party();
            sender.setName(GREEN);
            pModeProvider.getSenderParty(pModeKey);
            result = sender;

            Party receiver = new Party();
            receiver.setName(RED);
            pModeProvider.getReceiverParty(pModeKey);
            result = receiver;

            Party confParty = new Party();
            confParty.setName(GREEN);

            pModeProvider.getGatewayParty();
            result = confParty;

            Mpc mpc = new Mpc();
            mpc.setName(Ebms3Constants.DEFAULT_MPC);

            LegConfiguration legConfiguration = new LegConfiguration();
            legConfiguration.setDefaultMpc(mpc);
            legConfiguration.setErrorHandling(new ErrorHandling());

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;
        }};

        final String messageId = messageSubmitterImpl.submit(messageData, BACKEND);
        assertEquals(MESS_ID, messageId);

        new Verifications() {{
            authUtils.getOriginalUserWithUnsecureLoginAllowed();
            messageIdGenerator.generateMessageId();
            pModeProvider.getLegConfiguration(anyString);
            messagingService.storeMessagePayloads(withAny(new UserMessage()), null, MSHRole.SENDING, withAny(new LegConfiguration()), anyString);
            userMessageService.scheduleSending((UserMessage) any, (UserMessageLog) any);
            times = 0;
        }};

    }

    @Test
    public void testSubmitMessageWithIdNOk(@Injectable final Submission submission) throws Exception {
        String messageId = "abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656@domibus.eu";

        new Expectations() {{
            submission.getMessageId();
            result = messageId;

            backendMessageValidator.validateSubmissionSending(submission);
            result = EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0008)
                    .message("MessageId value is too long (over 255 characters)")
                    .build();
        }};

        try {
            messageSubmitterImpl.submit(submission, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (MessagingProcessingException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(ErrorCode.EBMS_0008, mpEx.getEbms3ErrorCode());
        }

        new FullVerifications() {{
            authUtils.getOriginalUserWithUnsecureLoginAllowed();
            times = 1;

            authUtils.isUnsecureLoginAllowed();
            times = 1;

            authUtils.hasUserOrAdminRole();
            times = 1;

            errorLogService.createErrorLog((EbMS3Exception) any, MSHRole.SENDING, null);
            times = 1;
        }};

    }

}
