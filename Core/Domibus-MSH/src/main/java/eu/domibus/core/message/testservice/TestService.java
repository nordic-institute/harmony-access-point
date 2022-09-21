package eu.domibus.core.message.testservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.model.*;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.model.configuration.Agreement;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.error.ErrorLogEntry;
import eu.domibus.core.error.ErrorLogService;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.ProcessingType;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.handler.MessageSubmitter;
import eu.domibus.web.rest.ro.TestServiceMessageInfoRO;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_MONITORING_CONNECTION_DELETE_HISTORY_FOR_PARTIES;

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

    private static final String BACKEND_NAME = "TestService";

    private final PModeProvider pModeProvider;

    private final MessageSubmitter messageSubmitter;

    private final UserMessageLogDao userMessageLogDao;

    private final UserMessageDao userMessageDao;

    private final SignalMessageDao signalMessageDao;

    private final ErrorLogService errorLogService;

    private final UserMessageService userMessageService;

    private final DomibusPropertyProvider domibusPropertyProvider;

    public TestService(PModeProvider pModeProvider, MessageSubmitter messageSubmitter, UserMessageLogDao userMessageLogDao, UserMessageDao userMessageDao,
                       SignalMessageDao signalMessageDao, ErrorLogService errorLogService,
                       UserMessageService userMessageService, DomibusPropertyProvider domibusPropertyProvider) {
        this.pModeProvider = pModeProvider;
        this.messageSubmitter = messageSubmitter;
        this.userMessageLogDao = userMessageLogDao;
        this.userMessageDao = userMessageDao;
        this.signalMessageDao = signalMessageDao;
        this.errorLogService = errorLogService;
        this.userMessageService = userMessageService;
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    public String submitTest(String sender, String receiver) throws IOException, MessagingProcessingException {
        LOG.info("Submitting test message from [{}] to [{}]", sender, receiver);

        Submission messageData = createSubmission(sender);

        // Set Receiver
        messageData.getToParties().clear();
        messageData.addToParty(receiver, pModeProvider.getPartyIdType(receiver));

        String result = messageSubmitter.submit(messageData, BACKEND_NAME);

        deleteSentHistoryIfApplicable(receiver);

        return result;
    }

    public String submitTestDynamicDiscovery(String sender, String receiver, String receiverType) throws MessagingProcessingException, IOException {
        LOG.info("Submitting test message with dynamic discovery from [{}] to [{}] with type [{}]", sender, receiver, receiverType);

        Submission messageData = createSubmission(sender);

        // Clears Receivers
        messageData.getToParties().clear();

        // Set Final Recipient Value and Type
        for (Submission.TypedProperty property : messageData.getMessageProperties()) {
            if (property.getKey().equals(MESSAGE_PROPERTY_KEY_FINAL_RECIPIENT)) {
                property.setValue(receiver);
                property.setType(receiverType);
            }
        }

        return messageSubmitter.submit(messageData, BACKEND_NAME);
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

    /**
     * This method is to get the last test Sent User Message for the given party Id,
     * including errors if not found
     *
     * @param partyId
     * @return TestServiceMessageInfoRO
     * @throws TestServiceException
     */
    public TestServiceMessageInfoRO getLastTestSentWithErrors(String partyId) throws TestServiceException {
        TestServiceMessageInfoRO result = getLastTestSent(partyId);
        if (result == null) {
            throw new TestServiceException(DomibusCoreErrorCode.DOM_001, "No User message found for party [" + partyId + "]");
        }

        if (result.getTimeReceived() == null) {
            String errorDetails = getErrorsDetails(result.getMessageId());
            throw new TestServiceException("No User Message found. Error details are: " + errorDetails);
        }

        return result;
    }

    /**
     * This method retrieves the last test Sent User Message for the given party Id
     *
     * @param partyId
     * @return TestServiceMessageInfoRO
     * @throws TestServiceException
     */
    public TestServiceMessageInfoRO getLastTestSent(String partyId) {
        LOG.debug("Getting last sent test message for partyId [{}]", partyId);

        UserMessage userMessage = userMessageDao.findLastTestMessageToParty(partyId);
        if (userMessage == null) {
            LOG.debug("Could not find last user message for party [{}]", partyId);
            return null;
        }

        UserMessageLog userMessageLog = userMessageLogDao.findByEntityId(userMessage.getEntityId());
        return getTestServiceMessageInfoRO(partyId, userMessage.getMessageId(), userMessageLog);
    }

    /**
     * This method is to get the last Received Signal Message for a test message for the given party Id and User MessageId,
     * including errors if an acceptable signal message cannot be found.
     *
     * @param partyId, userMessageId
     * @return TestServiceMessageInfoRO
     * @throws TestServiceException
     */
    public TestServiceMessageInfoRO getLastTestReceivedWithErrors(String partyId, String userMessageId) throws TestServiceException {
        TestServiceMessageInfoRO result = getLastTestReceived(partyId, userMessageId);
        if (result == null) {
            String errorDetails = getErrorsDetails(userMessageId);
            throw new TestServiceException("No Signal Message found. " + errorDetails);
        }

        return result;
    }

    /**
     * This method retrieves the last Received Signal Message for a test message for the given party Id and User MessageId
     *
     * @param partyId, userMessageId
     * @return TestServiceMessageInfoRO
     */
    public TestServiceMessageInfoRO getLastTestReceived(String partyId, String userMessageId) {
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
            // if userMessageId is not provided, find the most recent signal message received for a test message
            signalMessage = signalMessageDao.findLastTestMessage(partyId);
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
            LOG.warn("Could not delete old test messages from party [{}]", party, ex);
        }

    }

    protected String getErrorsDetails(String userMessageId) {
        String result;
        String errorDetails = getErrorsForMessage(userMessageId);
        if (StringUtils.isEmpty(errorDetails)) {
            result = "Please call the method again to see the details.";
        } else {
            result = "Error details: " + errorDetails;
        }
        return result;
    }

    protected String getErrorsForMessage(String userMessageId) {
        List<ErrorLogEntry> errorLogEntries = errorLogService.getErrorsForMessage(userMessageId);
        return errorLogEntries.stream()
                .map(err -> err.getErrorCode().getErrorCodeName() + "-" + err.getErrorDetail())
                .collect(Collectors.joining(", "));
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

    protected void deleteSentHistoryIfApplicable(String toParty) {
        List<String> partyList = domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_MONITORING_CONNECTION_DELETE_HISTORY_FOR_PARTIES);
        if (!partyList.contains(toParty)) {
            LOG.debug("Deleting sent test message history for party [{}] is not enabled", toParty);
            return;
        }

        LOG.debug("Deleting sent test message history for toParty [{}]", toParty);
        List<UserMessage> userMessages = findSentMessagesToKeep(toParty);
        List<UserMessage> all = userMessageDao.findTestMessagesToParty(toParty);

        try {
            deleteByDifference(userMessages, all);
        } catch (Exception ex) {
            LOG.warn("Could not delete old test messages to party [{}]", toParty, ex);
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
            LOG.debug("Adding the last received successful message [{}]", lastReceivedSuccess.getMessageId());
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
            LOG.debug("Could not find last received user message for party [{}]", partyId);
            return null;
        }
        return userMessage;
    }

    private void deleteByDifference(List<UserMessage> except, List<UserMessage> all) {
        List<Long> toDelete = all.stream()
                .filter(el -> except.stream().noneMatch(el1 -> el1.getEntityId() == el.getEntityId()))
                .map(el -> el.getEntityId())
                .collect(Collectors.toList());
        LOG.debug("Deleting messages with ids [{}]", toDelete);
        userMessageService.deleteMessagesWithIDs(toDelete);
    }

}
