package eu.domibus.core.message;

import eu.domibus.test.AbstractIT;
import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.model.*;
import eu.domibus.common.MessageDaoTestUtil;
import eu.domibus.core.message.dictionary.*;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.test.common.MessageTestUtility;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static eu.domibus.common.model.configuration.Identifier_.PARTY_ID_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Transactional
public class UserMessageDaoTestIT extends AbstractIT {

    private static final String STRING_TYPE = "string";

    @Autowired
    UserMessageDao userMessageDao;

    @Autowired
    SignalMessageDao signalMessageDao;

    @Autowired
    protected MpcDao mpcDao;

    @Autowired
    protected MshRoleDao mshRoleDao;

    @Autowired
    protected PartyIdDao partyIdDao;

    @Autowired
    protected PartyRoleDao partyRoleDao;

    @Autowired
    protected ActionDao actionDao;

    @Autowired
    protected ServiceDao serviceDao;

    @Autowired
    protected AgreementDao agreementDao;

    @Autowired
    MessagePropertyDao propertyDao;

    @Autowired
    MessageDaoTestUtil messageDaoTestUtil;

    @Test
    @Transactional
    public void testSaveUserMessage() {
        final MessageTestUtility messageTestUtility = new MessageTestUtility();
        final UserMessage userMessage = messageTestUtility.createSampleUserMessage();

        MessageProperty messageProperty1 = propertyDao.findOrCreateProperty("originalSender", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1", STRING_TYPE);
        MessageProperty messageProperty2 = propertyDao.findOrCreateProperty("finalRecipient", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4", STRING_TYPE);
        userMessage.setMessageProperties(new HashSet<>(Arrays.asList(messageProperty1, messageProperty2)));

        PartyId senderPartyId = messageTestUtility.createSenderPartyId();
        PartyId persistedSenderPartyId = partyIdDao.findOrCreateParty(senderPartyId.getValue(), senderPartyId.getType());
        userMessage.getPartyInfo().getFrom().setFromPartyId(persistedSenderPartyId);

        final PartyRole senderPartyRole = messageTestUtility.createSenderPartyRole();
        PartyRole persistedSender   = partyRoleDao.findOrCreateRole(senderPartyRole.getValue());
        userMessage.getPartyInfo().getFrom().setFromRole(persistedSender);

        final PartyId receiverPartyId = messageTestUtility.createReceiverPartyId();
        PartyId persistedReceiverPartyId = partyIdDao.findOrCreateParty(receiverPartyId.getValue(), receiverPartyId.getType());
        userMessage.getPartyInfo().getTo().setToPartyId(persistedReceiverPartyId);

        final PartyRole receiverPartyRole = messageTestUtility.createReceiverPartyRole();
        PartyRole persistedReceiver = partyRoleDao.findOrCreateRole(receiverPartyRole.getValue());
        userMessage.getPartyInfo().getTo().setToRole(persistedReceiver);

        final ActionEntity actionEntity = messageTestUtility.createActionEntity();
        ActionEntity persistedAction = actionDao.findOrCreateAction(actionEntity.getValue());
        userMessage.setAction(persistedAction);

        final ServiceEntity serviceEntity = messageTestUtility.createServiceEntity();
        ServiceEntity persistedService = serviceDao.findOrCreateService(serviceEntity.getValue(), serviceEntity.getType());
        userMessage.setService(persistedService);

        final AgreementRefEntity agreementRefEntity = messageTestUtility.createAgreementRefEntity();
        AgreementRefEntity persistedAgreementRef = agreementDao.findOrCreateAgreement(agreementRefEntity.getValue(), agreementRefEntity.getType());
        userMessage.setAgreementRef(persistedAgreementRef);

        final MpcEntity mpcEntity = messageTestUtility.createMpcEntity();
        MpcEntity persistedMpc = mpcDao.findOrCreateMpc(mpcEntity.getValue());
        userMessage.setMpc(persistedMpc);

        MSHRoleEntity sendingRole = mshRoleDao.findOrCreate(MSHRole.SENDING);
        userMessage.setMshRole(sendingRole);

        userMessageDao.create(userMessage);

        final UserMessage dbUserMessage = userMessageDao.findByEntityId(userMessage.getEntityId());
        assertNotNull(dbUserMessage);
        final Set<MessageProperty> msgProperties = dbUserMessage.getMessageProperties();
        msgProperties.forEach(messageProperty -> assertNotNull(messageProperty.getValue()));

        assertNotNull( userMessage.getPartyInfo().getFrom().getFromRole().getValue());
    }

    @Test
    @Transactional
    public void testFindLastTestMessageId() {
        UserMessageLog testMessageOlder = messageDaoTestUtil.createTestMessage(this.getClass().getName() + "msg-test-0");
        String msgId = this.getClass().getName() + "msg-test-1";
        UserMessageLog testMessage = messageDaoTestUtil.createTestMessage(msgId);

        String testParty = testMessage.getUserMessage().getPartyInfo().getToParty(); // "domibus-red"
        String senderPartyId = "domibus-blue";
        String type = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
        PartyId fromPartyId = partyIdDao.findExistingPartyId(senderPartyId, type);
        PartyId toPartyId = partyIdDao.findExistingPartyId(testParty, type);
        UserMessage userMessage = userMessageDao.findLastTestMessageFromPartyToParty(fromPartyId, toPartyId);
        assertNotNull(userMessage);
        assertEquals(msgId, userMessage.getMessageId());

        SignalMessage signalMessage = signalMessageDao.findLastTestMessage(fromPartyId, toPartyId);
        assertNotNull(signalMessage);
        assertEquals(msgId, signalMessage.getRefToMessageId());
        assertEquals(msgId, signalMessage.getUserMessage().getMessageId());

        messageDaoTestUtil.deleteMessages(Arrays.asList(testMessageOlder.getEntityId(), testMessage.getEntityId()));
    }

}
