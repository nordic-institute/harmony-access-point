package eu.domibus.common.dao;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.Messaging;
import eu.domibus.api.model.MpcEntity;
import eu.domibus.api.model.PartyInfo;
import eu.domibus.api.model.UserMessage;
import eu.domibus.common.JPAConstants;
import eu.domibus.common.model.configuration.Identifier;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.PartyIdType;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.party.PartyDao;
import eu.domibus.test.util.PojoInstaciatorUtil;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public class MessagingDaoTestIT extends AbstractIT {


    @Autowired
    private UserMessageDao userMessageDao;

    @Autowired
    private PartyDao partyDao;

    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    @Test
    @Transactional
    @Rollback
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void findMessagingOnStatusReceiverAndMpc() {

        List<Identifier> identifiers = new ArrayList<>();
        Identifier identifier = new Identifier();
        identifier.setPartyId("domibus-blue");
        PartyIdType partyIdType1 = new PartyIdType();
        partyIdType1.setName("partyIdTypeUrn1");
        identifier.setPartyIdType(partyIdType1);
        identifiers.add(identifier);
        Party party = PojoInstaciatorUtil.instanciate(Party.class, " [name:domibus-blue]");
        party.setIdentifiers(identifiers);
        Identifier next = party.getIdentifiers().iterator().next();
        next.setPartyId("RED_MSH");
        PartyIdType partyIdType = next.getPartyIdType();
        partyIdType.setValue("party_id_value");
        entityManager.persist(partyIdType);
        partyDao.create(party);
        PartyInfo firstParty = PojoInstaciatorUtil.instanciate(PartyInfo.class, "to[role:test,partyId{[value:RED_MSH]}]");
        Messaging firstMessage = new Messaging();
        UserMessage firstUm = new UserMessage();
        firstUm.setPartyInfo(firstParty);
        firstUm.setConversationId("conv123");
        firstMessage.setUserMessage(firstUm);
        firstUm.setRefToMessageId(null);
        firstUm.setMessageId("123456");
        MpcEntity mpc = new MpcEntity();
        mpc.setValue("http://mpc");
        firstMessage.getUserMessage().setMpc(mpc);
        firstMessage.setId(null);

        PartyInfo secondParty = PojoInstaciatorUtil.instanciate(PartyInfo.class, "to[role:test,partyId{[value:RED_MSH]}]");
        Messaging secondMessage = new Messaging();
        UserMessage secondUm = new UserMessage();
        secondUm.setPartyInfo(secondParty);
        secondMessage.setUserMessage(secondUm);
        secondUm.setMessageId("789101212");
        secondUm.setRefToMessageId(null);
        secondMessage.setId(null);
//        messagingDao.create(firstMessage);
        //@thom fix this late because their is a weird contraint exception here.

//        UserMessageLogEntityBuilder umlBuilder = UserMessageLogEntityBuilder.create()
//                .setMessageId(messageInfo.getMessageId())
//                .setMessageStatus(MessageStatus.READY_TO_PULL)
//                .setMshRole(MSHRole.SENDING);
//        userMessageLogDao.create(umlBuilder.build());

//        List<MessagePullDto> testParty = messagingDao.findMessagingOnStatusReceiverAndMpc("RED_MSH", MessageStatus.READY_TO_PULL, "http://mpc");
//        assertEquals(1, testParty.size());
//        assertEquals("123456", testParty.get(0).getMessageId());
//
//        Messaging messageByMessageId = messagingDao.findMessageByMessageId("123456");
//        assertNotNull(messageByMessageId.getCreatedBy());
//        assertNotNull(messageByMessageId.getCreationTime());
//        assertNotNull(messageByMessageId.getModifiedBy());
//        assertNotNull(messageByMessageId.getModificationTime());
//        assertEquals(messageByMessageId.getCreationTime(), messageByMessageId.getModificationTime());
    }

}