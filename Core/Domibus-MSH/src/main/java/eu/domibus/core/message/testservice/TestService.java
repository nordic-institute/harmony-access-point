package eu.domibus.core.message.testservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.model.*;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.model.configuration.Agreement;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.error.ErrorLogEntry;
import eu.domibus.core.error.ErrorLogService;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.monitoring.ConnectionMonitoringHelper;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.ProcessingType;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.handler.MessageSubmitter;
import eu.domibus.web.rest.ro.TestErrorsInfoRO;
import eu.domibus.web.rest.ro.TestMessageErrorRo;
import eu.domibus.web.rest.ro.TestServiceMessageInfoRO;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Cosmin Baciu
 * @author Ion Perpegel
 * @since 4.0
 */
@Service
public class TestService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TestService.class);

    private static final String TEST_PAYLOAD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><hello>world</hello>";

    private static final String MESSAGE_PROPERTY_KEY_FINAL_RECIPIENT = "finalRecipient";

    public static final String BACKEND_NAME = "TestService";

    private final PModeProvider pModeProvider;

    private final MessageSubmitter messageSubmitter;

    private final UserMessageLogDao userMessageLogDao;

    private final UserMessageDao userMessageDao;

    private final SignalMessageDao signalMessageDao;

    private final ErrorLogService errorLogService;

    private final UserMessageService userMessageService;

    private final ConnectionMonitoringHelper connectionMonitoringHelper;

    public TestService(PModeProvider pModeProvider, MessageSubmitter messageSubmitter, UserMessageLogDao userMessageLogDao, UserMessageDao userMessageDao,
                       SignalMessageDao signalMessageDao, ErrorLogService errorLogService,
                       UserMessageService userMessageService, ConnectionMonitoringHelper connectionMonitoringHelper) {
        this.pModeProvider = pModeProvider;
        this.messageSubmitter = messageSubmitter;
        this.userMessageLogDao = userMessageLogDao;
        this.userMessageDao = userMessageDao;
        this.signalMessageDao = signalMessageDao;
        this.errorLogService = errorLogService;
        this.userMessageService = userMessageService;
        this.connectionMonitoringHelper = connectionMonitoringHelper;
    }

    public String submitTest(String senderParty, String receiverParty) throws IOException, MessagingProcessingException {
        LOG.info("Submitting test message from [{}] to [{}]", senderParty, receiverParty);

        connectionMonitoringHelper.validateSender(senderParty);
        connectionMonitoringHelper.validateReceiver(receiverParty);

        Submission messageData = createSubmission(senderParty);

        // Set Receiver
        messageData.getToParties().clear();
        messageData.addToParty(receiverParty, pModeProvider.getPartyIdType(receiverParty));

        String result = messageSubmitter.submit(messageData, BACKEND_NAME);

        deleteSentHistory(receiverParty);

        return result;
    }

    public String submitTestDynamicDiscovery(String senderParty, String receiverParty, String receiverType) throws MessagingProcessingException, IOException {
        LOG.info("Submitting test message with dynamic discovery from [{}] to [{}] with type [{}]", senderParty, receiverParty, receiverType);

        connectionMonitoringHelper.validateSender(senderParty);

        Submission messageData = createSubmission(senderParty);

        // Clears Receivers
        messageData.getToParties().clear();

        // Set Final Recipient Value and Type
        for (Submission.TypedProperty property : messageData.getMessageProperties()) {
            if (property.getKey().equals(MESSAGE_PROPERTY_KEY_FINAL_RECIPIENT)) {
                property.setValue(receiverParty);
                property.setType(receiverType);
            }
        }

        return messageSubmitter.submit(messageData, BACKEND_NAME);
    }

    /**
     * This method is to get the last test Sent User Message for the given party Id,
     * including errors if not found
     *
     * @param partyId
     * @return TestServiceMessageInfoRO
     * @throws TestServiceException
     */
    public TestServiceMessageInfoRO getLastTestSentWithErrors(String senderPartyId, String partyId) throws TestServiceException {
        TestServiceMessageInfoRO result = getLastTestSent(senderPartyId, partyId);
        if (result == null) {
            throw new TestServiceException(DomibusCoreErrorCode.DOM_001, "No User message found for party [" + partyId + "]");
        }

        if (result.getTimeReceived() == null) {
            TestErrorsInfoRO errorDetails = getErrorsDetails(result.getMessageId());
            errorDetails.setMessage("No user message response found.");
            throw new TestServiceException(errorDetails);
        }

        return result;
    }

    /**
     * This method retrieves the last test Sent User Message for the given party Id
     *
     * @param senderPartyId
     * @param partyId
     * @return TestServiceMessageInfoRO
     * @throws TestServiceException
     */
    public TestServiceMessageInfoRO getLastTestSent(String senderPartyId, String partyId) {
        LOG.debug("Getting last sent test message for partyId [{}]", partyId);

        UserMessage userMessage = userMessageDao.findLastTestMessageFromPartyToParty(senderPartyId, partyId);
        if (userMessage == null) {
            LOG.debug("Could not find last test user message sent for party [{}]", partyId);
            return null;
        }

        UserMessageLog userMessageLog = userMessageLogDao.findByEntityId(userMessage.getEntityId());
        return getTestServiceMessageInfoRO(partyId, userMessage.getMessageId(), userMessageLog);
    }

    /**
     * This method is to get the last Received Signal Message for a test message for the given party Id and User MessageId,
     * including errors if an acceptable signal message cannot be found.
     *
     * @param partyId
     * @param userMessageId
     * @param senderPartyId
     * @return TestServiceMessageInfoRO
     * @throws TestServiceException
     */
    public TestServiceMessageInfoRO getLastTestReceivedWithErrors(String senderPartyId, String partyId, String userMessageId) throws TestServiceException {
        TestServiceMessageInfoRO result = getLastTestReceived(senderPartyId, partyId, userMessageId);
        if (result == null) {
            TestErrorsInfoRO errorDetails = getErrorsDetails(userMessageId);
            throw new TestServiceException("No Signal Message found. " + errorDetails);
        }

        return result;
    }

    /**
     * This method retrieves the last Received Signal Message for a test message for the given party Id and User MessageId
     *
     * @param partyId
     * @param userMessageId
     * @param senderPartyId
     * @return TestServiceMessageInfoRO
     */
    public TestServiceMessageInfoRO getLastTestReceived(String senderPartyId, String partyId, String userMessageId) {
        LOG.debug("Getting last received signal for a test message from partyId [{}]", partyId);

        SignalMessage signalMessage;

        if (StringUtils.isNotBlank(userMessageId)) {
            // if userMessageId is provided, try to find its signal message
            signalMessage = signalMessageDao.findByUserMessageIdWithUserMessage(userMessageId, MSHRole.SENDING);
            if (signalMessage == null) {
                LOG.debug("Could not find messaging for message ID [{}]", userMessageId);
                return null;
            }
        } else {
            signalMessage = signalMessageDao.findLastTestMessage(senderPartyId, partyId);
            if (signalMessage == null) {
                LOG.debug("Could not find any signal message from party [{}]", partyId);
                return null;
            }
        }

        return getTestServiceMessageInfoRO(partyId, signalMessage);
    }

    public void deleteReceivedMessageHistoryFromParty(String party) {
        LOG.debug("Deleting received test messages for party [{}]", party);
        List<UserMessage> userMessages = findReceivedMessagesToKeep(party);
        List<UserMessage> all = userMessageDao.findTestMessagesFromParty(party);
        try {
            deleteByDifference(userMessages, all);
        } catch (Exception ex) {
            LOG.warn("Could not delete test message history from party [{}]", party, ex);
        }
    }

    public TestErrorsInfoRO getErrorsDetails(String userMessageId) {
        TestErrorsInfoRO result;
        TestErrorsInfoRO errorDetails = getErrorsForMessage(userMessageId);
        if (errorDetails == null) {
            result = new TestErrorsInfoRO("Please call the method again to see the details.");
        } else {
            errorDetails.setMessage("Errors for the test message with id " + userMessageId);
            result = errorDetails;
        }
        return result;
    }

    protected Submission createSubmission(String sender) throws IOException {
        Resource testServiceFile = new ClassPathResource("messages/testservice/testservicemessage.json");
        String jsonStr = new String(IOUtils.toByteArray(testServiceFile.getInputStream()), StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        Submission submission = mapper.readValue(jsonStr, Submission.class);

        DataHandler payLoadDataHandler = new DataHandler(new ByteArrayDataSource(TEST_PAYLOAD.getBytes(), "text/xml"));
        Submission.TypedProperty objTypedProperty = new Submission.TypedProperty("MimeType", "text/xml");
        Collection<Submission.TypedProperty> listTypedProperty = new ArrayList<>();
        listTypedProperty.add(objTypedProperty);
        Submission.Payload objPayload1 = new Submission.Payload("cid:message", payLoadDataHandler, listTypedProperty, false, null, null);
        submission.addPayload(objPayload1);

        // Set Sender
        submission.getFromParties().clear();
        submission.addFromParty(sender, pModeProvider.getPartyIdType(sender));

        // Set ServiceType
        submission.setServiceType(pModeProvider.getServiceType(Ebms3Constants.TEST_SERVICE));

        // Set From Role
        submission.setFromRole(pModeProvider.getRole("INITIATOR", Ebms3Constants.TEST_SERVICE));

        // Set To Role
        submission.setToRole(pModeProvider.getRole("RESPONDER", Ebms3Constants.TEST_SERVICE));

        // Set Agreement Ref
        Agreement agreementRef = pModeProvider.getAgreementRef(Ebms3Constants.TEST_SERVICE);
        if (agreementRef != null) {
            submission.setAgreementRef(agreementRef.getValue());
            submission.setAgreementRefType(agreementRef.getType());
        }

        // Set Conversation Id
        // As the eb:ConversationId element is required it must always have a value.
        // If no value is included in the 301 submission of the business document to the Access Point,
        // the Access Point MUST set the value of 302 eb:ConversationId to “1” as specified in section 4.3 of [ebMS3CORE].
        submission.setConversationId("1");

        submission.setProcessingType(ProcessingType.PUSH);

        return submission;
    }

    protected TestErrorsInfoRO getErrorsForMessage(String userMessageId) {
        List<ErrorLogEntry> errorLogEntries = errorLogService.getErrorsForMessage(userMessageId);
        if (CollectionUtils.isEmpty(errorLogEntries)) {
            LOG.debug("No error log entries found for message with id [{}]", userMessageId);
            return null;
        }
        return new TestErrorsInfoRO(
                errorLogEntries.stream()
                        .map(err -> new TestMessageErrorRo(err.getErrorCode().getErrorCodeName(), err.getErrorDetail()))
                        .collect(Collectors.toList())
        );
    }

    protected TestServiceMessageInfoRO getTestServiceMessageInfoRO(String partyId, SignalMessage signalMessage) {
        TestServiceMessageInfoRO messageInfoRO = new TestServiceMessageInfoRO();
        if (signalMessage != null) {
            messageInfoRO.setMessageId(signalMessage.getSignalMessageId());
            messageInfoRO.setTimeReceived(signalMessage.getTimestamp());
            messageInfoRO.setMshRole(signalMessage.getUserMessage().getMshRole().getRole());
        }

        messageInfoRO.setPartyId(partyId);
        Party party = pModeProvider.getPartyByIdentifier(partyId);
        if (party != null) {
            messageInfoRO.setAccessPoint(party.getEndpoint());
        }
        return messageInfoRO;
    }

    protected TestServiceMessageInfoRO getTestServiceMessageInfoRO(String partyId, String userMessageId, UserMessageLog userMessageLog) {
        TestServiceMessageInfoRO messageInfoRO = new TestServiceMessageInfoRO();
        messageInfoRO.setMessageId(userMessageId);
        if (userMessageLog != null) {
            messageInfoRO.setMessageStatus(userMessageLog.getMessageStatus());
            messageInfoRO.setTimeReceived(userMessageLog.getReceived());
            messageInfoRO.setMshRole(userMessageLog.getMshRole().getRole());
        }

        messageInfoRO.setPartyId(partyId);
        Party party = pModeProvider.getPartyByIdentifier(partyId);
        if (party != null) {
            messageInfoRO.setAccessPoint(party.getEndpoint());
        }
        return messageInfoRO;
    }

    protected void deleteSentHistory(String toParty) {
        List<String> partyList = connectionMonitoringHelper.getDeleteHistoryForParties();
        if (partyList.stream().noneMatch(pair -> StringUtils.equals(connectionMonitoringHelper.getDestinationParty(pair), toParty))) {
            LOG.debug("Deleting sent test message history for party [{}] is not enabled", toParty);
            return;
        }

        LOG.debug("Deleting sent test message history for toParty [{}]", toParty);
        List<UserMessage> userMessages = findSentMessagesToKeep(toParty);
        List<UserMessage> all = userMessageDao.findTestMessagesToParty(toParty);

        try {
            deleteByDifference(userMessages, all);
        } catch (Exception ex) {
            LOG.warn("Could not delete test message history to party [{}]", toParty, ex);
        }
    }

    private List<UserMessage> findSentMessagesToKeep(String toParty) {
        List<UserMessage> userMessages = new ArrayList<>();

        // find last successful message
        UserMessage lastSentSuccess = getLastTestSentWithStatus(toParty, MessageStatus.ACKNOWLEDGED);
        if (lastSentSuccess != null) {
            LOG.debug("Adding the last successful message [{}]", lastSentSuccess.getMessageId());
            userMessages.add(lastSentSuccess);
        }

        // find last unsuccessful message newer that the successful one
        UserMessage lastSentError = getLastTestSentWithStatus(toParty, MessageStatus.SEND_FAILURE);
        if (lastSentError != null) {
            if (lastSentSuccess == null || lastSentError.getTimestamp().after(lastSentSuccess.getTimestamp())) {
                LOG.debug("Adding the last sent message with error [{}]", lastSentError.getMessageId());
                userMessages.add(lastSentError);
            }
        }

        // find pending message newer than any of those before
        UserMessage lastSentPending = getLastTestSentWithStatus(toParty, MessageStatus.SEND_ENQUEUED);
        if (lastSentPending != null) {
            if (userMessages.isEmpty() || userMessages.get(userMessages.size() - 1).getTimestamp().before(lastSentPending.getTimestamp())) {
                LOG.debug("Adding the last pending message [{}]", lastSentPending.getMessageId());
                userMessages.add(lastSentPending);
            }
        }

        return userMessages;
    }

    private List<UserMessage> findReceivedMessagesToKeep(String party) {
        List<UserMessage> userMessages = new ArrayList<>();

        // find last received message
        UserMessage lastReceivedSuccess = getLastTestReceived(party);
        if (lastReceivedSuccess != null) {
            LOG.debug("Adding the last received message [{}]", lastReceivedSuccess.getMessageId());
            userMessages.add(lastReceivedSuccess);
        }

        return userMessages;
    }

    protected UserMessage getLastTestSentWithStatus(String partyId, MessageStatus messageStatus) {
        UserMessage userMessage = userMessageDao.findLastTestMessageToPartyWithStatus(partyId, messageStatus);
        if (userMessage == null) {
            LOG.debug("Could not find last sent user message for party [{}]", partyId);
            return null;
        }
        return userMessage;
    }

    private UserMessage getLastTestReceived(String partyId) {
        UserMessage userMessage = userMessageDao.findLastTestMessageFromParty(partyId);
        if (userMessage == null) {
            LOG.debug("Could not find any received user message from party [{}]", partyId);
            return null;
        }
        return userMessage;
    }

    private void deleteByDifference(List<UserMessage> except, List<UserMessage> all) {
        List<Long> toDelete = all.stream()
                .filter(el -> except.stream().noneMatch(el1 -> el1.getEntityId() == el.getEntityId()))
                .map(AbstractBaseEntity::getEntityId)
                .collect(Collectors.toList());
        LOG.debug("Deleting messages with ids [{}]", toDelete);
        userMessageService.deleteMessagesWithIDs(toDelete);
    }

}
