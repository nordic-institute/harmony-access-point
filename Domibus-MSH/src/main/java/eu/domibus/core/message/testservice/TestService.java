package eu.domibus.core.message.testservice;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.TestServiceException;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.ebms3.common.model.Ebms3Constants;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.handler.DatabaseMessageHandler;
import eu.domibus.web.rest.ro.TestServiceMessageInfoRO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.persistence.NoResultException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class TestService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TestService.class);

    private static final String TEST_PAYLOAD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><hello>world</hello>";

    private static final String MESSAGE_PROPERTY_KEY_FINAL_RECIPIENT = "finalRecipient";

    private static final String BACKEND_NAME = "TestService";

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private DatabaseMessageHandler databaseMessageHandler;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private MessagingDao messagingDao;

    public String submitTest(String sender, String receiver) throws IOException, MessagingProcessingException {
        LOG.info("Submitting test message from [{}] to [{}]", sender, receiver);

        Submission messageData = createSubmission(sender);

        // Set Receiver
        messageData.getToParties().clear();
        messageData.addToParty(receiver, pModeProvider.getPartyIdType(receiver));

        return databaseMessageHandler.submit(messageData, BACKEND_NAME);
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

        return databaseMessageHandler.submit(messageData, BACKEND_NAME);
    }

    protected Submission createSubmission(String sender) throws IOException {
        Resource testServiceFile = new ClassPathResource("messages/testservice/testservicemessage.xml");
        XStream xstream = new XStream();
        xstream.addPermission(NoTypePermission.NONE);
        xstream.addPermission(PrimitiveTypePermission.PRIMITIVES);
        xstream.addPermission(new ExplicitTypePermission(new Class[]{List.class, Submission.class, Submission.TypedProperty.class, Submission.Party.class}));

        Submission submission = (Submission) xstream.fromXML(testServiceFile.getInputStream());
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
        submission.setAgreementRef(pModeProvider.getAgreementRef(Ebms3Constants.TEST_SERVICE));

        // Set Conversation Id
        // As the eb:ConversationId element is required it must always have a value.
        // If no value is included in the 301 submission of the business document to the Access Point,
        // the Access Point MUST set the value of 302 eb:ConversationId to “1” as specified in section 4.3 of [ebMS3CORE].
        submission.setConversationId("1");

        return submission;
    }

    /**
     * This method is to get the last Sent User Message for the given party Id
     *
     * @param partyId
     * @return TestServiceMessageInfoRO
     * @throws TestServiceException
     */
    public TestServiceMessageInfoRO getLastTestSent(String partyId) throws TestServiceException {
        LOG.debug("Getting last sent test message for partyId='{}'", partyId);

        String userMessageId = userMessageLogDao.findLastUserTestMessageId(partyId);
        if (StringUtils.isBlank(userMessageId)) {
            LOG.debug("Could not find last user message id for party [{}]", partyId);
            throw new TestServiceException("No User message id  found for the sending party [" + partyId + "]");
        }

        UserMessageLog userMessageLog = null;
        //TODO create a UserMessageLog object independent of Hibernate annotations in the domibus-api and use the UserMessageLogService instead
        try {
            userMessageLog = userMessageLogDao.findByMessageId(userMessageId);
        } catch (NoResultException ex) {
            LOG.trace("No UserMessageLog found for message with id [{}]", userMessageId);
        }

        if (userMessageLog != null) {
            TestServiceMessageInfoRO testServiceMessageInfoRO = new TestServiceMessageInfoRO();
            testServiceMessageInfoRO.setMessageId(userMessageId);
            testServiceMessageInfoRO.setTimeReceived(userMessageLog.getReceived());
            testServiceMessageInfoRO.setPartyId(partyId);
            Party party = pModeProvider.getPartyByIdentifier(partyId);
            testServiceMessageInfoRO.setAccessPoint(party.getEndpoint());

            return testServiceMessageInfoRO;
        }

        throw new TestServiceException("No User Message found for message Id [" + userMessageId + "]");
    }

    /**
     * This method is to get the last Received Signal Message for the given party Id and User MessageId
     *
     * @param partyId, userMessageId
     * @return TestServiceMessageInfoRO
     * @throws TestServiceException
     */
    public TestServiceMessageInfoRO getLastTestReceived(String partyId, String userMessageId) throws TestServiceException {
        LOG.debug("Getting last received test message from partyId='{}'", partyId);

        Messaging messaging = messagingDao.findMessageByMessageId(userMessageId);
        if (messaging == null) {
            LOG.debug("Could not find messaging for message ID[{}]", userMessageId);
            throw new TestServiceException("No User Message found for message Id [" + userMessageId + "]");
        }

        SignalMessage signalMessage = messaging.getSignalMessage();
        if (signalMessage != null) {
            TestServiceMessageInfoRO testServiceMessageInfoRO = new TestServiceMessageInfoRO();
            testServiceMessageInfoRO.setMessageId(signalMessage.getMessageInfo().getMessageId());
            testServiceMessageInfoRO.setTimeReceived(signalMessage.getMessageInfo().getTimestamp());
            Party party = pModeProvider.getPartyByIdentifier(partyId);
            testServiceMessageInfoRO.setPartyId(partyId);
            testServiceMessageInfoRO.setAccessPoint(party.getEndpoint());

            return testServiceMessageInfoRO;
        }
        throw new TestServiceException("No Signal Message found for the message Id [" + userMessageId + "]");
    }

}
