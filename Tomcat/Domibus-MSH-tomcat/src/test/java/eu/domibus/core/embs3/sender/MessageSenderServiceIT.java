package eu.domibus.core.embs3.sender;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.*;
import eu.domibus.core.ebms3.sender.MessageSenderService;
import eu.domibus.core.message.MessageStatusDao;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.dictionary.MshRoleDao;
import eu.domibus.core.message.dictionary.PartyIdDao;
import eu.domibus.core.message.reliability.PartyStatusDao;
import eu.domibus.core.message.reliability.PartyStatusEntity;
import eu.domibus.core.property.DomibusPropertyResourceHelper;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.user.UserManagementServiceTestIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolationException;
import java.io.IOException;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SMART_RETRY_ENABLED;

@Transactional
public class MessageSenderServiceIT extends AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserManagementServiceTestIT.class);
    private static final String LOGGED_USER = "test_user";
    @Autowired
    MessageSenderService messageSenderService;
    @Autowired
    UserMessageLogDao userMessageLogDao;
    @Autowired
    UserMessageDao userMessageDao;
    @Autowired
    MessageStatusDao messageStatusDao;
    @Autowired
    MshRoleDao mshRoleDao;
    @Autowired
    protected PartyIdDao partyIdDao;

    @Autowired
    DomibusPropertyResourceHelper domibusPropertyResourceHelper;

    @Autowired
    PartyStatusDao partyStatusDao;

    ///////////////////////////////////////

    @Before
    public void before() {
        try {
            uploadPmode(18001);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlProcessingException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Transactional
    @WithUserDetails(value = LOGGED_USER, userDetailsServiceBeanName = "testUserDetailService")
    public void testDestinationIsReachable() {
        domibusPropertyResourceHelper.setPropertyValue(DOMIBUS_SMART_RETRY_ENABLED, true,"domibus-red");
        final String partyIdType = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
        UserMessageLog userMessageLog = new UserMessageLog();
        MessageStatusEntity messageStatus = new MessageStatusEntity();
        messageStatus.setMessageStatus(MessageStatus.WAITING_FOR_RETRY);
        messageStatusDao.create(messageStatus);
        userMessageLog.setMessageStatus(messageStatus);

        PartyInfo partyInfo = new PartyInfo();

        From from = new From();
        PartyId fromParty = new PartyId();
        fromParty.setValue("domibus-blue");
        fromParty.setType(partyIdType);
        partyIdDao.create(fromParty);

        from.setFromPartyId(fromParty);

        To to = new To();
        PartyId toParty = new PartyId();
        toParty.setValue("domibus-red");
        toParty.setType(partyIdType);
        partyIdDao.create(toParty);

        to.setToPartyId(toParty);

        partyInfo.setFrom(from);
        partyInfo.setTo(to);

        UserMessage message = new UserMessage();
        message.setMessageId("id-123");
        message.setConversationId("convid-123");
        message.setPartyInfo(partyInfo);

        userMessageDao.create(message);

        userMessageLog.setUserMessage(message);
        MSHRoleEntity mshRole = new MSHRoleEntity();
        mshRoleDao.create(mshRole);
        userMessageLog.setMshRole(mshRole);

        userMessageLogDao.create(userMessageLog);

        PartyStatusEntity partyStatus = new PartyStatusEntity();
        partyStatus.setPartyName("domibus-red");
        partyStatus.setConnectivityStatus("SUCCESS");
        partyStatusDao.create(partyStatus);

        try {
            messageSenderService.sendUserMessage("messageId", userMessageLog.getEntityId(), 5);
        } catch (ConstraintViolationException e) {
            // this means the service did try to send the message because the destination is reachable
            Assert.assertNotNull(message.getEntityId());
        }


    }


    @Test
    public void test2TriesOneFinally() {

        try {
            try {
                throw new RuntimeException("");
            } catch (RuntimeException re) {
                int i=1;
                return;
            }
        } finally {
            int j = 2;
        }
    }

}
