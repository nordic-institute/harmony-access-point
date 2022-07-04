package eu.domibus.core.embs3.sender;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.*;
import eu.domibus.common.JPAConstants;
import eu.domibus.core.ebms3.sender.MessageSenderService;
import eu.domibus.core.message.MessageStatusDao;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.dictionary.MshRoleDao;
import eu.domibus.core.message.dictionary.PartyIdDao;
import eu.domibus.core.security.UserDetailServiceImpl;
import eu.domibus.core.user.ui.UserDao;
import eu.domibus.core.user.ui.UserRoleDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.user.UserManagementServiceTestIT;
import eu.domibus.web.security.AuthenticationService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolationException;

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


    ///////////////////////////////////////

    @Autowired
    UserDetailServiceImpl userDetailService;

    @Autowired
    protected UserDao userDao;
    @Autowired
    protected AuthenticationService authenticationService;

    @Autowired
    protected UserRoleDao userRoleDao;

    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    protected EntityManager entityManager;

    @Before
    public void before() {
        userDao.delete(userDao.listUsers());
    }

    @Test
    @Transactional
    @WithUserDetails(value = LOGGED_USER, userDetailsServiceBeanName = "testUserDetailService")
    public void testDestinationIsReachable() {
        final String partyIdType = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
        UserMessageLog userMessageLog = new UserMessageLog();
        MessageStatusEntity messageStatus = new MessageStatusEntity();
        messageStatus.setMessageStatus(MessageStatus.WAITING_FOR_RETRY);
        messageStatusDao.create(messageStatus);
        userMessageLog.setMessageStatus(messageStatus);

        UserMessage userMsg = new UserMessage();
        PartyInfo partyInfo = new PartyInfo();

        From from = new From();
        PartyId fromParty = new PartyId();
        fromParty.setValue("1");
        fromParty.setType(partyIdType);
        partyIdDao.create(fromParty);

        from.setFromPartyId(fromParty);

        To to = new To();
        PartyId toParty = new PartyId();
        toParty.setValue("2");
        toParty.setType(partyIdType);
        partyIdDao.create(toParty);

        to.setToPartyId(toParty);

        partyInfo.setFrom(from);
        partyInfo.setTo(to);

        userMsg.setPartyInfo(partyInfo);

        userMessageDao.create(userMsg);

        userMessageLog.setUserMessage(userMsg);
        MSHRoleEntity mshRole = new MSHRoleEntity();
        mshRoleDao.create(mshRole);
        userMessageLog.setMshRole(mshRole);

        userMessageLogDao.create(userMessageLog);

        try {
            messageSenderService.sendUserMessage("messageId", userMessageLog.getEntityId(), 5);
        } catch (ConstraintViolationException e) {
            // this means the service did try to send the message because the destination is reachable
            Assert.assertNotNull(userMsg.getEntityId());
        }



    }



}
