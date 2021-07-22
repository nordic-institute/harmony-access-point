package eu.domibus.core.plugin.handler;

import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.model.*;
import eu.domibus.api.pmode.PModeConstants;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.ErrorResultImpl;
import eu.domibus.common.model.configuration.*;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.error.ErrorService;
import eu.domibus.core.generator.id.MessageIdGenerator;
import eu.domibus.core.message.*;
import eu.domibus.core.message.compression.CompressionException;
import eu.domibus.core.message.compression.CompressionService;
import eu.domibus.core.message.dictionary.MpcDictionaryService;
import eu.domibus.core.message.dictionary.MshRoleDao;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.message.splitandjoin.SplitAndJoinService;
import eu.domibus.core.payload.PayloadProfileValidator;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.core.plugin.transformer.SubmissionAS4Transformer;
import eu.domibus.core.pmode.PModeDefaultService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.pmode.validation.validators.MessagePropertyValidator;
import eu.domibus.core.pmode.validation.validators.PropertyProfileValidator;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.*;
import eu.domibus.plugin.Submission;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;

import javax.jms.Queue;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Federico Martini
 * @since 3.2
 * <p>
 * in the Verifications() the execution "times" is by default 1.
 */
@RunWith(JMockit.class)
public class DatabaseMessageHandlerTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DatabaseMessageHandlerTest.class);
    private static final String BACKEND = "backend";
    private static final String DEF_PARTY_TYPE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    private static final String STRING_TYPE = "string";
    private static final String MESS_ID = UUID.randomUUID().toString();
    private static final String DOMIBUS_GREEN = "domibus-green";
    private static final String DOMIBUS_RED = "domibus-red";
    private static final String GREEN = "green_gw";
    private static final String RED = "red_gw";
    private static final String BLUE = "blue_gw";
    private static final String AGREEMENT = "";
    private static final String SERVICE = "testService1";
    private static final String ACTION = "TC2Leg1";
    private static final String LEG = "pushTestcase1tc2Action";

    private String pModeKey = GREEN + PModeConstants.PMODEKEY_SEPARATOR +
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
    private UIReplicationSignalService uiReplicationSignalService;

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
    private ErrorService errorService;

    @Injectable
    private PartInfoService partInfoService;

    @Tested
    private DatabaseMessageHandler databaseMessageHandler;

    protected static MessageProperty createProperty(String name, String value, String type) {
        MessageProperty aProperty = new MessageProperty();
        aProperty.setValue(value);
        aProperty.setName(name);
        aProperty.setType(type);
        return aProperty;
    }

    protected static UserMessage createUserMessage() {
        UserMessage userMessage = new UserMessage();
        ActionEntity action = new ActionEntity();
        action.setValue("TC2Leg1");
        userMessage.setAction(action);

        AgreementRefEntity agreementRef1 = new AgreementRefEntity();
        agreementRef1.setValue("");
        userMessage.setAgreementRef(agreementRef1);

        ServiceEntity service1 = new ServiceEntity();
        service1.setValue("bdx:noprocess");
        service1.setType("tc1");
        userMessage.setService(service1);

        HashSet<MessageProperty> messageProperties1 = new HashSet<>();
        messageProperties1.add(createProperty("originalSender", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1", STRING_TYPE));
        messageProperties1.add(createProperty("finalRecipient", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4", STRING_TYPE));
        userMessage.setMessageProperties(messageProperties1);

        PartyInfo partyInfo = new PartyInfo();

        From from = new From();
        from.setFromRole(getRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator"));

        from.setFromPartyId(new PartyId());
        from.getFromPartyId().setValue(DOMIBUS_GREEN);
        from.getFromPartyId().setType(DEF_PARTY_TYPE);
        partyInfo.setFrom(from);

        To to = new To();
        to.setToRole(getRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder"));
        to.setToPartyId(new PartyId());
        to.getToPartyId().setValue(DOMIBUS_RED);
        to.getToPartyId().setType(DEF_PARTY_TYPE);
        partyInfo.setTo(to);

        userMessage.setPartyInfo(partyInfo);

//        PayloadInfo payloadInfo = new PayloadInfo();
//        PartInfo partInfo = new PartInfo();
//        partInfo.setHref("cid:message");

//        HashSet<PartProperty> partProperties1 = new HashSet<>();
//        partProperties1.add(createPartProperty("text/xml", "MimeType", STRING_TYPE))
//        partInfo.setPartProperties(partProperties1);

//        payloadInfo.getPartInfo().add(partInfo);
//        userMessage.setPayloadInfo(payloadInfo);
        return userMessage;
    }

    private static PartyRole getRole(String value) {
        PartyRole partyRole = new PartyRole();
        partyRole.setValue(value);
        return partyRole;
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testSubmitMessageGreen2RedOk(@Injectable final Submission messageData,
                                             @Injectable PartInfo partInfo,
                                             @Injectable MessageExchangeConfiguration messageExchangeConfiguration,
                                             @Injectable Party sender,
                                             @Injectable Party receiver,
                                             @Injectable Party confParty) throws Exception {
        final UserMessage userMessage = new UserMessage();
        new Expectations() {{
            authUtils.getOriginalUserFromSecurityContext();
            result = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";

            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            result = messageExchangeConfiguration;

            messageExchangeConfiguration.getPmodeKey();
            result = pModeKey;

            messageExchangeService.getMessageStatus(messageExchangeConfiguration);
            result = MessageStatus.SEND_ENQUEUED;

            pModeProvider.getSenderParty(pModeKey);
            result = sender;

            pModeProvider.getReceiverParty(pModeKey);
            result = receiver;

            pModeProvider.getGatewayParty();
            result = confParty;
            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;
        }};

        final String messageId = databaseMessageHandler.submit(messageData, BACKEND);
        assertEquals(MESS_ID, messageId);

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext();
            messageIdGenerator.generateMessageId();
            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING);
            pModeProvider.getLegConfiguration(pModeKey);
            messagePropertyValidator.validate(withAny(new UserMessage()), MSHRole.SENDING);
            messagingService.storeMessagePayloads(withAny(new UserMessage()), null, MSHRole.SENDING, withAny(new LegConfiguration()), anyString);
            userMessageLogService.save(withAny(new UserMessage()), anyString, anyString, MSHRole.SENDING.toString(), anyInt, anyString);
            userMessageService.scheduleSending(userMessage, (UserMessageLog) any);
        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testSubmitPullMessageGreen2RedOk(@Injectable final Submission messageData, @Injectable PartInfo partInfo) throws Exception {
        new Expectations() {{

            authUtils.getOriginalUserFromSecurityContext();
            result = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";

            UserMessage userMessage = createUserMessage();
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration("", "green_gw", "red_gw", "testService1", "TC2Leg1", "pushTestcase1tc2Action");
            result = messageExchangeConfiguration;

            messageExchangeService.getMessageStatus(messageExchangeConfiguration);
            result = MessageStatus.READY_TO_PULL;

            Party sender = new Party();
            sender.setName(GREEN);
            pModeProvider.getSenderParty(pModeKey);
            result = sender;

            Party receiver = new Party();
            receiver.setName(RED);
            pModeProvider.getReceiverParty(pModeKey);
            result = receiver;

            Configuration conf = new Configuration();
            Party confParty = new Party();
            confParty.setName(GREEN);
            conf.setParty(confParty);

            pModeProvider.getGatewayParty();
            result = confParty;

            Mpc mpc = new Mpc();
            mpc.setName(Ebms3Constants.DEFAULT_MPC);

            LegConfiguration legConfiguration = new LegConfiguration();
            final Map<Party, Mpc> mpcMap = new HashMap<>();
            mpcMap.put(receiver, mpc);
            legConfiguration.setDefaultMpc(mpc);
            legConfiguration.setErrorHandling(new ErrorHandling());

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;
        }};

        final String messageId = databaseMessageHandler.submit(messageData, BACKEND);
        assertEquals(MESS_ID, messageId);

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext();
            messageIdGenerator.generateMessageId();
            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING);
            pModeProvider.getLegConfiguration(anyString);
            UserMessage message;
//            assertEquals("TC2Leg1", message.getCollaborationInfo().getAction());
//            assertEquals("bdx:noprocess", message.getCollaborationInfo().getService().getValue());
            messagePropertyValidator.validate(withAny(new UserMessage()), MSHRole.SENDING);
            messagingService.storeMessagePayloads(withAny(new UserMessage()), null, MSHRole.SENDING, withAny(new LegConfiguration()), anyString);
            userMessageLogService.save(withAny(new UserMessage()), MessageStatus.READY_TO_PULL.toString(), anyString, MSHRole.SENDING.toString(), anyInt, anyString);
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
            result = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0008, "MessageId value is too long (over 255 characters)", null, null);
        }};

        try {
            databaseMessageHandler.submit(submission, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (MessagingProcessingException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(ErrorCode.EBMS_0008, mpEx.getEbms3ErrorCode());
        }

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext();
            messageIdGenerator.generateMessageId();
            times = 0;
            userMessageLogService.getMessageStatus(MESS_ID);
            times = 0;
            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING);
            times = 0;
            pModeProvider.getLegConfiguration(anyString);
            times = 0;
            messagingService.storeMessagePayloads(withAny(new UserMessage()), null, MSHRole.SENDING, legConfiguration, anyString);
            times = 0;
        }};

    }


    @Test
    public void testSubmitMessageWithRefIdNOk(@Injectable final Submission submission) throws Exception {
        String refToMessageId = "abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656@domibus.eu";

        new Expectations() {{
            backendMessageValidator.validateSubmissionSending(submission);
            result = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0008, "RefToMessageId value is too long (over 255 characters)", refToMessageId, null);
        }};

        try {
            databaseMessageHandler.submit(submission, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (MessagingProcessingException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(ErrorCode.EBMS_0008, mpEx.getEbms3ErrorCode());
        }

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext();
            userMessageLogService.getMessageStatus(MESS_ID);
            times = 0;
            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING);
            times = 0;
            pModeProvider.getLegConfiguration(anyString);
            times = 0;
            messagingService.storeMessagePayloads(withAny(new UserMessage()), null, MSHRole.SENDING, legConfiguration, anyString);
            times = 0;
        }};

    }

    @Test
    public void testSubmitMessageGreen2RedNOk(@Injectable final Submission messageData) throws Exception {
        new Expectations() {{

            UserMessage userMessage = createUserMessage();
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            result = new MessageExchangeConfiguration("", "green_gw", "red_gw", "testService1", "TC2Leg1", "pushTestcase1tc2Action");

            // Here the configuration of the access point is supposed to be BLUE!
            Party confParty = new Party();
            confParty.setName(BLUE);

            pModeProvider.getGatewayParty();
            result = confParty;

            backendMessageValidator.validateInitiatorParty(withAny(new Party()), withAny(new Party()));
            result = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "The initiator party's name [" + GREEN + "] does not correspond to the access point's name [" + BLUE + "]", null, null);

        }};

        try {
            databaseMessageHandler.submit(messageData, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (MessagingProcessingException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(ErrorCode.EBMS_0010, mpEx.getEbms3ErrorCode());
            assert (mpEx.getMessage().contains("does not correspond to the access point's name"));
        }

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext();
            messageIdGenerator.generateMessageId();
            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING);
            backendMessageValidator.validateParties(withAny(new Party()), withAny(new Party()));
            backendMessageValidator.validateInitiatorParty(withAny(new Party()), withAny(new Party()));
            backendMessageValidator.validateResponderParty(withAny(new Party()), withAny(new Party()));
            times = 0;
            pModeProvider.getLegConfiguration(anyString);
            times = 0;
            messagePropertyValidator.validate(withAny(new UserMessage()), MSHRole.SENDING);
            times = 0;
            messagingService.storeMessagePayloads(withAny(new UserMessage()), null, MSHRole.SENDING, legConfiguration, anyString);
            times = 0;
        }};

    }

    @Test
    /* Tests a submit message where from and to parties are the same. */
    public void testSubmitMessageBlue2BlueNOk(@Injectable final Submission messageData) throws Exception {
        new Expectations() {{

            UserMessage userMessage = createUserMessage();
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            backendMessageValidator.validateParties(withAny(new Party()), withAny(new Party()));
            result = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "The initiator party's name is the same as the responder party's one", null, null);
        }};

        try {
            databaseMessageHandler.submit(messageData, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (MessagingProcessingException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(ErrorCode.EBMS_0010, mpEx.getEbms3ErrorCode());
            assert (mpEx.getMessage().contains("The initiator party's name is the same as the responder party's one"));
        }

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext();
            messageIdGenerator.generateMessageId();
            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING);
            backendMessageValidator.validateParties(withAny(new Party()), withAny(new Party()));

            backendMessageValidator.validateInitiatorParty(withAny(new Party()), withAny(new Party()));
            times = 0;
            backendMessageValidator.validateResponderParty(withAny(new Party()), withAny(new Party()));
            times = 0;
            pModeProvider.getLegConfiguration(anyString);
            times = 0;
            messagingService.storeMessagePayloads(withAny(new UserMessage()), null, MSHRole.SENDING, legConfiguration, anyString);
            times = 0;
        }};
    }


    @Test
    public void testSubmitMessagePModeNOk(@Injectable final Submission messageData) throws Exception {
        new Expectations() {{

            UserMessage userMessage = createUserMessage();
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            result = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "PMode could not be found. Are PModes configured in the database?", MESS_ID, null);
        }};

        try {
            databaseMessageHandler.submit(messageData, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (MessagingProcessingException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(ErrorCode.EBMS_0010, mpEx.getEbms3ErrorCode());
            assert (mpEx.getMessage().contains("PMode could not be found. Are PModes configured in the database?"));
        }

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext();
            messageIdGenerator.generateMessageId();
            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING);
            pModeProvider.getLegConfiguration(anyString);
            times = 0;
            messagingService.storeMessagePayloads(withAny(new UserMessage()), null, MSHRole.SENDING, legConfiguration, anyString);
            times = 0;
        }};
    }

    @Test
    public void testSubmitPullMessagePModeNOk(@Injectable final Submission messageData) throws Exception {
        new Expectations() {{
            UserMessage userMessage = createUserMessage();
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration("", "green_gw", "red_gw", "testService1", "TC2Leg1", "pushTestcase1tc2Action");
            result = messageExchangeConfiguration;
            ;

            messageExchangeService.getMessageStatus(messageExchangeConfiguration);
            result = new PModeException(DomibusCoreErrorCode.DOM_003, "invalid pullprocess configuration");
        }};

        try {
            databaseMessageHandler.submit(messageData, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (PModeMismatchException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(ErrorCode.EBMS_0010, mpEx.getEbms3ErrorCode());
            assert (mpEx.getMessage().contains("invalid pullprocess configuration"));
        }

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext();
            messageIdGenerator.generateMessageId();
            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING);
            pModeProvider.getLegConfiguration(anyString);
            messagingService.storeMessagePayloads(withAny(new UserMessage()), null, MSHRole.SENDING, legConfiguration, anyString);
            userMessageService.scheduleSending((UserMessage) any, (UserMessageLog) any);
            times = 0;
        }};
    }

    @Test
    public void testSubmitDuplicateMessage(@Injectable final Submission submission, @Injectable UserMessage userMessage) throws Exception {
        new Expectations() {{
            backendMessageValidator.validateSubmissionSending(submission);
            result = new DuplicateMessageException("Message with id [" + MESS_ID + "] already exists. Message identifiers must be unique");
        }};

        try {
            databaseMessageHandler.submit(submission, BACKEND);
            Assert.fail("It should throw " + DuplicateMessageException.class.getCanonicalName());
        } catch (DuplicateMessageException ex) {
            LOG.debug("DuplicateMessageException catched: " + ex.getMessage());
            assert (ex.getMessage().contains("already exists. Message identifiers must be unique"));
        }

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext();
        }};
    }

    @Test
    public void testVerifyOriginalUserNOk(@Injectable final Submission messageData,
                                          @Injectable UserMessage userMessage) throws Exception {

        String originalUser = "mycorner";
        new Expectations(databaseMessageHandler) {{
            authUtils.getOriginalUserFromSecurityContext();
            result = originalUser;

            transformer.transformFromSubmission(messageData);
            result = userMessage;

            databaseMessageHandler.validateOriginalUser(userMessage, originalUser, MessageConstants.ORIGINAL_SENDER);
            result = new AccessDeniedException("You are not allowed to handle this message. You are authorized as [" + originalUser + "]");
        }};

        try {
            databaseMessageHandler.submit(messageData, BACKEND);
            Assert.fail("It should throw " + AccessDeniedException.class.getCanonicalName());
        } catch (AccessDeniedException ex) {
            LOG.debug("AccessDeniedException catched: " + ex.getMessage());
            assertTrue(ex.getMessage().contains("You are not allowed to handle this message. You are authorized as [mycorner]"));
        }

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext();
        }};
    }


    @Test
    public void testSubmitMessageStoreNOk(@Injectable final Submission messageData, @Injectable PartInfo partInfo) throws Exception {
        new Expectations() {{
            authUtils.getOriginalUserFromSecurityContext();
            result = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";

            UserMessage userMessage = createUserMessage();
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
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
            databaseMessageHandler.submit(messageData, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (MessagingProcessingException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(ErrorCode.EBMS_0303, mpEx.getEbms3ErrorCode());
            assert (mpEx.getMessage().contains("Could not store binary data for message due to IO exception"));
        }

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext();
            messageIdGenerator.generateMessageId();
            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING);
            pModeProvider.getLegConfiguration(anyString);
            messagingService.storeMessagePayloads(withAny(new UserMessage()), null, MSHRole.SENDING, legConfiguration, anyString);
        }};
    }


    public void testStoreMessageToBePulled(@Injectable final Submission messageData, @Injectable PartInfo partInfo) throws EbMS3Exception {
        new Expectations() {{

            authUtils.getOriginalUserFromSecurityContext();
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

            Party confParty = new Party();
            confParty.setName(GREEN);

            Mpc mpc = new Mpc();
            mpc.setName(Ebms3Constants.DEFAULT_MPC);

            LegConfiguration legConfiguration = new LegConfiguration();
            final Map<Party, Mpc> mpcMap = new HashMap<>();
            mpcMap.put(receiver, mpc);
            legConfiguration.setDefaultMpc(mpc);
            legConfiguration.setErrorHandling(new ErrorHandling());

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            compressionService.handleCompression(MESS_ID, partInfo, legConfiguration);
            result = true;

            messageExchangeService.getMessageStatus(messageExchangeConfiguration);
            result = MessageStatus.READY_TO_PULL;

        }};

    }

    @Test
    public void testValidateOriginalUserOK(@Injectable final UserMessage userMessage) throws Exception {
        String originalUser = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4";
        List<String> recipients = new ArrayList<>();
        recipients.add(MessageConstants.ORIGINAL_SENDER);
        recipients.add(MessageConstants.FINAL_RECIPIENT);

        new Expectations() {{
            userMessageServiceHelper.getOriginalUser(userMessage, MessageConstants.ORIGINAL_SENDER);
            result = originalUser;
        }};

        databaseMessageHandler.validateOriginalUser(userMessage, originalUser, recipients);
    }

    @Test(expected = AccessDeniedException.class)
    public void testValidateOriginalUserNoFR() throws Exception {
        String originalUser = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4";

        final UserMessage userMessage = createUserMessage();

        List<String> recipients = new ArrayList<>();
        recipients.add(MessageConstants.ORIGINAL_SENDER);

        databaseMessageHandler.validateOriginalUser(userMessage, originalUser, recipients);
    }

    @Test(expected = AccessDeniedException.class)
    public void testValidateOriginalUserNoMatch() throws Exception {
        String originalUser = "nobodywho";

        final UserMessage userMessage = createUserMessage();

        List<String> recipients = new ArrayList<>();
        recipients.add(MessageConstants.ORIGINAL_SENDER);
        recipients.add(MessageConstants.FINAL_RECIPIENT);

        databaseMessageHandler.validateOriginalUser(userMessage, originalUser, recipients);
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testDownloadMessageOK(@Injectable UserMessage userMessage,
                                      @Injectable UserMessageLog userMessageLog,
                                      @Injectable Submission submission,
                                      @Injectable Messaging messaging) throws Exception {

        new Expectations(databaseMessageHandler) {{

            userMessageLogService.findByMessageId(MESS_ID);
            result = userMessageLog;

            databaseMessageHandler.shouldDeleteDownloadedMessage(userMessage, null);
            result = false;

            transformer.transformFromMessaging(userMessage, null);
            result = submission;

        }};

        final Submission sub = databaseMessageHandler.downloadMessage(MESS_ID);

        new Verifications() {{
            authUtils.hasUserOrAdminRole();
            userMessageLogService.setMessageAsDownloaded(userMessage, userMessageLog);

        }};

    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testDownloadMessageOK_RetentionNonZero(@Injectable Messaging messaging,
                                                       @Injectable UserMessage userMessage,
                                                       @Injectable final UserMessageLog messageLog) throws Exception {
        new Expectations(databaseMessageHandler) {{
            messaging.getUserMessage();
            result = userMessage;

            userMessageLogService.findByMessageId(MESS_ID);
            result = messageLog;

            databaseMessageHandler.shouldDeleteDownloadedMessage(userMessage, null);
            result = false;
        }};

        databaseMessageHandler.downloadMessage(MESS_ID);

        new Verifications() {{
            userMessageLogService.setMessageAsDownloaded(userMessage, messageLog);
        }};

    }

    @Test
    public void testDownloadMessageAuthUserNok(@Injectable UserMessage userMessage,
                                               @Injectable final UserMessageLog messageLog) throws Exception {

        String originalUser = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";
        new Expectations(databaseMessageHandler) {{
            authUtils.isUnsecureLoginAllowed();
            result = false;

            authUtils.getOriginalUserFromSecurityContext();
            result = originalUser;

            databaseMessageHandler.validateOriginalUser(userMessage, originalUser, MessageConstants.FINAL_RECIPIENT);
            result = new AccessDeniedException("You are not allowed to handle this message");
        }};

        try {
            databaseMessageHandler.checkMessageAuthorization(userMessage);
            Assert.fail("It should throw " + AccessDeniedException.class.getCanonicalName());
        } catch (AccessDeniedException adEx) {
            LOG.debug("Expected :", adEx);
            assertTrue(adEx.getMessage().contains("You are not allowed to handle this message"));
        }

        new Verifications() {{
            authUtils.hasUserOrAdminRole();
            authUtils.getOriginalUserFromSecurityContext();
        }};

    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testDownloadMessageNoMsgFound() throws Exception {


        try {
            databaseMessageHandler.downloadMessage(MESS_ID);
            Assert.fail("It should throw " + MessageNotFoundException.class.getCanonicalName());
        } catch (MessageNotFoundException mnfEx) {
            LOG.debug("Expected :", mnfEx);
            assert (mnfEx.getMessage().contains("was not found"));
        }

        new Verifications() {{
            userMessageLogService.findByMessageId(MESS_ID);
            times = 0;
        }};

    }

    @Test
    public void testGetErrorsForMessageOk() {


        new Expectations() {{
            authUtils.isUnsecureLoginAllowed();
            result = false;

            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0008, "MessageId value is too long (over 255 characters)", MESS_ID, null);
            List<ErrorResult> list = new ArrayList<>();
            ErrorResultImpl errorLogEntry = new ErrorResultImpl();

            errorLogEntry.setErrorCode(ex.getErrorCodeObject());
            errorLogEntry.setErrorDetail(ex.getErrorDetail());
            errorLogEntry.setMessageInErrorId(ex.getRefToMessageId());
            errorLogEntry.setMshRole(eu.domibus.common.MSHRole.RECEIVING);
            
            list.add(errorLogEntry);

            errorService.getErrors(MESS_ID);
            result = list;

        }};

        final List<? extends ErrorResult> results = databaseMessageHandler.getErrorsForMessage(MESS_ID);


        new Verifications() {{
            authUtils.hasUserOrAdminRole();
            errorService.getErrors(MESS_ID);
            Assert.assertNotNull(results);
            ErrorResult errRes = results.iterator().next();
            Assert.assertEquals(ErrorCode.EBMS_0008, errRes.getErrorCode());
        }};

    }

    @Test
    public void testGetStatus() {
        // Given
        new Expectations() {{
            authUtils.isUnsecureLoginAllowed();
            result = false;

            userMessageLogService.getMessageStatus(MESS_ID);
            result = eu.domibus.common.MessageStatus.ACKNOWLEDGED;
        }};

        // When
        final eu.domibus.common.MessageStatus status = databaseMessageHandler.getStatus(MESS_ID);

        // Then
        new Verifications() {{
            authUtils.hasUserOrAdminRole();
            Assert.assertEquals(eu.domibus.common.MessageStatus.ACKNOWLEDGED, status);
        }};
    }

    @Test
    public void testGetStatusAccessDenied() {
        // Given
        new Expectations(databaseMessageHandler) {{
            authUtils.isUnsecureLoginAllowed();
            result = false;

            databaseMessageHandler.validateOriginalUser((UserMessage) any, anyString, (List<String>) any);
            result = new AccessDeniedException("");
        }};

        // When
        eu.domibus.common.MessageStatus status = null;
        try {
            status = databaseMessageHandler.getStatus(MESS_ID);
            Assert.fail("It should throw " + AccessDeniedException.class.getCanonicalName());
        } catch (AccessDeniedException ex) {
            // ok
        }

        eu.domibus.common.MessageStatus finalStatus = status;
        new Verifications() {{
            authUtils.hasUserOrAdminRole();
            Assert.assertNull(finalStatus);
        }};
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
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void browseMessage(@Injectable UserMessage userMessage,
                              @Injectable UserMessageLog userMessageLog) throws MessageNotFoundException {
        String messageId = "123";

        new Expectations(databaseMessageHandler) {{
            userMessageLogService.findByMessageId(messageId);
            result = userMessageLog;

            databaseMessageHandler.checkMessageAuthorization(userMessage);
        }};

        databaseMessageHandler.browseMessage(messageId);

        new Verifications() {{
            transformer.transformFromMessaging(userMessage, null);
        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void submitMessageFragmentTest(@Injectable UserMessage userMessage,
                                          @Mocked Messaging message,
                                          @Injectable MessageStatus messageStatus,
                                          @Injectable MSHRole mshRole,
                                          @Injectable ObjectFactory ebMS3Of,
                                          @Injectable MessageExchangeConfiguration userMessageExchangeConfiguration,
                                          @Injectable Party to,
                                          @Injectable LegConfiguration legConfiguration) throws EbMS3Exception, MessagingProcessingException {
        String backendName = "backendName";
        String messageId = UUID.randomUUID().toString();
        String pModeKey = "pmodeKey";

        new Expectations(databaseMessageHandler) {{
            userMessage.getMessageId();
            result = messageId;
            userMessageLogService.getMessageStatus(messageId);
            result = MessageStatus.NOT_FOUND;
            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            result = userMessageExchangeConfiguration;
            userMessageExchangeConfiguration.getPmodeKey();
            result = pModeKey;
            databaseMessageHandler.messageValidations(userMessage, null, pModeKey, backendName);
            result = to;
            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;
            messageExchangeService.getMessageStatus(userMessageExchangeConfiguration);
            result = messageStatus;
        }};

        databaseMessageHandler.submitMessageFragment(userMessage, null, null, backendName);

        new Verifications() {{
            messagingService.storeMessagePayloads(userMessage, null, MSHRole.SENDING, legConfiguration, backendName);
            times = 1;
            uiReplicationSignalService.userMessageSubmitted(withCapture());
            times = 1;
        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void downloadMessageShouldDeleteDownloadedMessageTest(@Injectable UserMessage userMessage,
                                                                 @Injectable UserMessageLog userMessageLog,
                                                                 @Injectable Submission submission,
                                                                 @Injectable Messaging messaging) throws Exception {

        new Expectations(databaseMessageHandler) {{
            userMessageLogService.findByMessageId(MESS_ID);
            result = userMessageLog;

            databaseMessageHandler.shouldDeleteDownloadedMessage(userMessage, null);
            result = true;

            transformer.transformFromMessaging(userMessage, null);
            result = submission;

        }};

        databaseMessageHandler.downloadMessage(MESS_ID);

        new Verifications() {{
            authUtils.hasUserOrAdminRole();
            times = 1;
            userMessageLogService.setMessageAsDeleted(userMessage, userMessageLog);
            times = 1;

        }};

    }
}
