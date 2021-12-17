package eu.domibus.core.message;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.*;
import eu.domibus.core.message.dictionary.*;
import eu.domibus.test.common.MessageTestUtility;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

public class UserMessageDaoTestIT extends AbstractIT {

    @Autowired
    UserMessageDao userMessageDao;

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
    protected MessagePropertyDao messagePropertyDao;


    @Test
    public void testSaveUserMessage() {
        final MessageTestUtility messageTestUtility = new MessageTestUtility();
        final UserMessage userMessage = messageTestUtility.createSampleUserMessage();
        final List<PartInfo> partInfoList = messageTestUtility.createPartInfoList(userMessage);

        PartyId senderPartyId = messageTestUtility.createSenderPartyId();
        userMessage.getPartyInfo().getFrom().setFromPartyId(partyIdDao.findOrCreateParty(senderPartyId.getValue(), senderPartyId.getType()));

        userMessage.getPartyInfo().getFrom().setFromRole(partyRoleDao.findOrCreateRole(messageTestUtility.createSenderPartyRole().getValue()));

        final PartyId receiverPartyId = messageTestUtility.createReceiverPartyId();
        userMessage.getPartyInfo().getTo().setToPartyId(partyIdDao.findOrCreateParty(receiverPartyId.getValue(), receiverPartyId.getType()));

        userMessage.getPartyInfo().getTo().setToRole(partyRoleDao.findOrCreateRole(messageTestUtility.createReceiverPartyRole().getValue()));

        userMessage.setAction(actionDao.findOrCreateAction(messageTestUtility.createActionEntity().getValue()));

        final ServiceEntity serviceEntity = messageTestUtility.createServiceEntity();
        userMessage.setService(serviceDao.findOrCreateService(serviceEntity.getValue(), serviceEntity.getType()));

        final AgreementRefEntity agreementRefEntity = messageTestUtility.createAgreementRefEntity();
        userMessage.setAgreementRef(agreementDao.findOrCreateAgreement(agreementRefEntity.getValue(), agreementRefEntity.getType()));

        userMessage.setMpc(mpcDao.findOrCreateMpc(messageTestUtility.createMpcEntity().getValue()));

        HashSet<MessageProperty> messageProperties = new HashSet<>();
        for (MessageProperty messageProperty : userMessage.getMessageProperties()) {
            messageProperties.add(messagePropertyDao.findOrCreateProperty(messageProperty.getName(), messageProperty.getValue(), messageProperty.getType()));
        }

        userMessage.setMessageProperties(messageProperties);

        userMessageDao.create(userMessage);

        final UserMessage dbUserMessage = userMessageDao.findByEntityId(userMessage.getEntityId());
        assertNotNull(dbUserMessage);
        final Set<MessageProperty> msgProperties = dbUserMessage.getMessageProperties();
        msgProperties.forEach(messageProperty -> assertNotNull(messageProperty.getValue()));

        assertNotNull( userMessage.getPartyInfo().getFrom().getFromRole().getValue());
    }
}
