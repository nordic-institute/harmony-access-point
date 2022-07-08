package eu.domibus.core.plugin.handler;

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
import eu.domibus.common.ErrorResult;
import eu.domibus.common.ErrorResultImpl;
import eu.domibus.common.model.configuration.*;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.error.ErrorLogService;
import eu.domibus.core.generator.id.MessageIdGenerator;
import eu.domibus.core.message.*;
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
import eu.domibus.core.plugin.transformer.SubmissionAS4Transformer;
import eu.domibus.core.pmode.PModeDefaultService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.pmode.validation.validators.MessagePropertyValidator;
import eu.domibus.core.pmode.validation.validators.PropertyProfileValidator;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.*;
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
import java.util.List;
import java.util.UUID;

import static eu.domibus.test.common.UserMessageSampleUtil.createUserMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Federico Martini
 * @since 3.2
 * <p>
 * in the Verifications() the execution "times" is by default 1.
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
@RunWith(JMockit.class)
public class DatabaseMessageHandlerTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DatabaseMessageHandlerTest.class);

    private static final String MESS_ID = UUID.randomUUID().toString();

    private static final String GREEN = "green_gw";
    private static final String RED = "red_gw";
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

    @Tested
    private DatabaseMessageHandler databaseMessageHandler;

    @Injectable
    protected UserMessageSecurityService userMessageSecurityService;

    @Injectable
    protected ApplicationEventPublisher applicationEventPublisher;

    @Injectable
    protected SplitAndJoinHelper splitAndJoinHelper;

    @Injectable
    MessageSubmitterService messageSubmitterService;

    public void testStoreMessageToBePulled(@Injectable final Submission messageData, @Injectable PartInfo partInfo,
                                           @Injectable MessageStatusEntity messageStatus) throws EbMS3Exception {
        new Expectations() {{

            authUtils.getOriginalUserWithUnsecureLoginAllowed();
            result = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";

            UserMessage userMessage = createUserMessage();
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            userMessageLogService.getMessageStatus(MESS_ID);
            result = MessageStatus.NOT_FOUND;


            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration("", "green_gw", "red_gw", "testService1", "TC2Leg1", "pushTestcase1tc2Action");
            result = messageExchangeConfiguration;

            Party sender = new Party();
            sender.setName(GREEN);
            pModeProvider.getSenderParty(pModeKey);
            result = sender;

            Party receiver = new Party();
            receiver.setName(RED);
            pModeProvider.getReceiverParty(pModeKey);
            result = receiver;

            pModeProvider.getReceiverParty(pModeKey);
            result = receiver;

            Mpc mpc = new Mpc();
            mpc.setName(Ebms3Constants.DEFAULT_MPC);

            LegConfiguration legConfiguration = new LegConfiguration();

            legConfiguration.setDefaultMpc(mpc);
            legConfiguration.setErrorHandling(new ErrorHandling());

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            compressionService.handleCompression(MESS_ID, partInfo, legConfiguration);
            result = true;

            messageExchangeService.getMessageStatus(messageExchangeConfiguration, ProcessingType.PUSH);
            result = messageStatus;

            messageStatus.getMessageStatus();
            result = MessageStatus.READY_TO_PULL;
        }};

    }


    @Test
    public void testDownloadMessageOK(@Injectable UserMessage userMessage,
                                      @Injectable UserMessageLog userMessageLog,
                                      @Injectable Submission submission,
                                      @Injectable Messaging messaging) throws Exception {

        new Expectations() {{

            userMessageService.getByMessageId(MESS_ID);
            result = userMessage;
            userMessageLogService.findById(anyLong);
            result = userMessageLog;

        }};

        databaseMessageHandler.downloadMessage(MESS_ID);

        new Verifications() {{
            authUtils.hasUserOrAdminRole();
            userMessageLogService.setMessageAsDownloaded(userMessage, userMessageLog);

        }};

    }

    @Test
    public void testDownloadMessageOK_RetentionNonZero(@Injectable Messaging messaging,
                                                       @Injectable UserMessage userMessage,
                                                       @Injectable final UserMessageLog messageLog) throws Exception {
        new Expectations(databaseMessageHandler) {{
            userMessageService.getByMessageId(MESS_ID);
            result = userMessage;

            userMessageLogService.findById(anyLong);
            result = messageLog;

        }};

        databaseMessageHandler.downloadMessage(MESS_ID);

        new Verifications() {{
            userMessageLogService.setMessageAsDownloaded(userMessage, messageLog);
        }};

    }

    @Test
    public void testDownloadMessageAuthUserNok(@Injectable UserMessage userMessage,
                                               @Injectable final UserMessageLog messageLog) {

        String originalUser = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";
        new Expectations(databaseMessageHandler) {{
            authUtils.isUnsecureLoginAllowed();
            result = false;

            authUtils.getOriginalUserWithUnsecureLoginAllowed();
            result = originalUser;

            userMessageSecurityService.validateUserAccessWithUnsecureLoginAllowed(userMessage, originalUser, MessageConstants.FINAL_RECIPIENT);
            result = new AccessDeniedException("You are not allowed to handle this message");
        }};

        try {
            databaseMessageHandler.checkMessageAuthorization(userMessage);
            Assert.fail("It should throw AccessDeniedException");
        } catch (AccessDeniedException adEx) {
            LOG.debug("Expected :", adEx);
            assertTrue(adEx.getMessage().contains("You are not allowed to handle this message"));
        }

        new Verifications() {{
            authUtils.hasUserOrAdminRole();
            authUtils.getOriginalUserWithUnsecureLoginAllowed();
        }};

    }

    @Test
    public void testDownloadMessageNoMsgFound() {
        new Expectations() {{
            userMessageService.getByMessageId(MESS_ID);
            result = new MessageNotFoundException();
        }};

        try {
            databaseMessageHandler.downloadMessage(MESS_ID);
            Assert.fail("It should throw " + MessageNotFoundException.class.getCanonicalName());
        } catch (MessageNotFoundException mnfEx) {
            //OK
        }

        new Verifications() {{
            userMessageLogService.findByMessageId(MESS_ID);
            times = 0;
        }};

    }

    @Test
    public void testGetErrorsForMessageOk() {


        new Expectations() {{

            EbMS3Exception ex = EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0008)
                    .message("MessageId value is too long (over 255 characters)")
                    .refToMessageId(MESS_ID)
                    .build();
            List<ErrorResult> list = new ArrayList<>();
            ErrorResultImpl errorLogEntry = new ErrorResultImpl();

            errorLogEntry.setErrorCode(ex.getErrorCodeObject());
            errorLogEntry.setErrorDetail(ex.getErrorDetail());
            errorLogEntry.setMessageInErrorId(ex.getRefToMessageId());
            errorLogEntry.setMshRole(eu.domibus.common.MSHRole.RECEIVING);

            list.add(errorLogEntry);

            errorLogService.getErrors(MESS_ID);
            result = list;

        }};

        final List<? extends ErrorResult> results = databaseMessageHandler.getErrorsForMessage(MESS_ID);


        new Verifications() {{
            userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(MESS_ID);

            errorLogService.getErrors(MESS_ID);
            Assert.assertNotNull(results);
            ErrorResult errRes = results.iterator().next();
            Assert.assertEquals(ErrorCode.EBMS_0008, errRes.getErrorCode());
        }};

    }

    @Test
    public void testGetStatus() {
        // Given
        new Expectations() {{


            userMessageLogService.getMessageStatus(MESS_ID);
            result = MessageStatus.ACKNOWLEDGED;
        }};

        // When
        final eu.domibus.common.MessageStatus status = databaseMessageHandler.getStatus(MESS_ID);

        Assert.assertEquals(eu.domibus.common.MessageStatus.ACKNOWLEDGED, status);

        new Verifications() {{
            userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(MESS_ID);
            times = 1;
        }};

    }

    @Test
    public void testGetStatusAccessDenied() {
        // Given
        new Expectations() {{
            userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(MESS_ID);
            result = new AccessDeniedException("");
        }};

        // When
        eu.domibus.common.MessageStatus status = null;
        try {
            status = databaseMessageHandler.getStatus(MESS_ID);
            Assert.fail("It should throw AccessDeniedException");
        } catch (AccessDeniedException ex) {
            // ok
        }

        Assert.assertNull(status);

    }

    @Test
    public void testcreateNewParty() {
        String mpc = "mpc_qn";
        String initiator = "initiator";
        // Given
        new Expectations() {{
            messageExchangeService.extractInitiator(mpc);
            result = initiator;
        }};
        Party party = databaseMessageHandler.createNewParty(mpc);
        Assert.assertNotNull(party);
        Assert.assertEquals(initiator, party.getName());
    }

    @Test
    public void testcreateNewPartyNull() {
        Party party = databaseMessageHandler.createNewParty(null);
        Assert.assertNull(party);
    }

    @Test
    public void browseMessage(@Injectable UserMessage userMessage,
                              @Injectable UserMessageLog userMessageLog) {
        String messageId = "123";

        new Expectations(databaseMessageHandler) {{
            userMessageService.getByMessageId(messageId);
            result = userMessage;


        }};

        databaseMessageHandler.browseMessage(messageId);

        new Verifications() {{
            databaseMessageHandler.checkMessageAuthorization(userMessage);
            messagingService.getSubmission(userMessage);
        }};
    }


}
