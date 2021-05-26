package eu.domibus.core.message;

import eu.domibus.AbstractIT;
import eu.domibus.common.MessageStatus;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author François Gautier
 * @since 5.0
 */
public class UserMessageLogDaoIT extends AbstractIT {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageLogDaoIT.class);
    public static final String MESSAGE_ID_SEND_FAILURE = UUID.randomUUID().toString();
    public static final String MESSAGE_ID_SEND_FAILURE_RECIPIENT = UUID.randomUUID().toString();
    public static final String MESSAGE_ID_ACKNOWLEDGED = UUID.randomUUID().toString();
    public static final String MESSAGE_ID_SEND_ENQUEUED = UUID.randomUUID().toString();
    public static final String MESSAGE_ID_SEND_ENQUEUED_RECIPIENT = UUID.randomUUID().toString();
    public static final ZonedDateTime ZONED_DATE_TIME_T1 = ZonedDateTime.of(2020, 1, 1, 12, 30, 30, 50, ZoneId.systemDefault());
    public static final ZonedDateTime ZONED_DATE_TIME_T2 = ZONED_DATE_TIME_T1.plusHours(1);

    @Autowired
    UserMessageLogDao userMessageLogDao;

    @Autowired
    UserMessageDao userMessageDao;

    @PersistenceContext(unitName = "domibusJTA")
    protected EntityManager em;

    @Before
    public void setup() {
        LOG.debug("Setting up");
    }


    @Test
    @Transactional
    public void testSaveMessageAcknowledge() {
        initialize();

        final List<String> entities = userMessageLogDao.findFailedMessages(null, null, null);
        assertEquals(2, entities.size());
        assertThat(entities, CoreMatchers.hasItems(MESSAGE_ID_SEND_FAILURE, MESSAGE_ID_SEND_FAILURE_RECIPIENT));

    }

    @Test
    @Transactional
    public void testSaveMessageAcknowledge_start() {
        initialize();

        final List<String> entities = userMessageLogDao.findFailedMessages(null, Date.from(ZONED_DATE_TIME_T1.plusMinutes(1).toInstant()), null);
        assertEquals(1, entities.size());
        assertThat(entities, CoreMatchers.hasItems(MESSAGE_ID_SEND_FAILURE_RECIPIENT));

    }

    @Test
    @Transactional
    public void testSaveMessageAcknowledge_finalRecipient() {
        initialize();

        final List<String> entities = userMessageLogDao.findFailedMessages("finalRecipient", null, null);
        assertEquals(1, entities.size());
        assertThat(entities, CoreMatchers.hasItems(MESSAGE_ID_SEND_FAILURE_RECIPIENT));

    }
    @Test
    @Transactional
    public void testSaveMessageAcknowledge_end() {
        initialize();

        final List<String> entities = userMessageLogDao.findFailedMessages(null, null, Date.from(ZONED_DATE_TIME_T1.plusMinutes(1).toInstant()));
        assertEquals(1, entities.size());
        assertThat(entities, CoreMatchers.hasItems(MESSAGE_ID_SEND_FAILURE));
    }

    @Test
    @Transactional
    public void testFindSendEnqueuedMessages() {
        initialize();

        final List<String> entities = userMessageLogDao.findSendEnqueuedMessages(null, null, null);
        assertEquals(2, entities.size());
        assertThat(entities, CoreMatchers.hasItems(MESSAGE_ID_SEND_ENQUEUED, MESSAGE_ID_SEND_ENQUEUED_RECIPIENT));

    }

    @Test
    @Transactional
    public void testFindSendEnqueuedMessages_startDate() {
        initialize();

        final List<String> entities = userMessageLogDao.findSendEnqueuedMessages(null, Date.from(ZONED_DATE_TIME_T1.plusMinutes(1).toInstant()), null);
        assertEquals(1, entities.size());
        assertThat(entities, CoreMatchers.hasItems(MESSAGE_ID_SEND_ENQUEUED_RECIPIENT));

    }

    @Test
    @Transactional
    public void testFindSendEnqueuedMessages_finalRecipient() {
        initialize();

        final List<String> entities = userMessageLogDao.findSendEnqueuedMessages("finalRecipient", null, null);
        assertEquals(1, entities.size());
        assertThat(entities, CoreMatchers.hasItems(MESSAGE_ID_SEND_ENQUEUED_RECIPIENT));

    }

    @Test
    @Transactional
    public void testFindSendEnqueuedMessages_start_endDate() {
        initialize();

        final List<String> entities = userMessageLogDao.findSendEnqueuedMessages(null, Date.from(ZONED_DATE_TIME_T1.toInstant()), Date.from(ZONED_DATE_TIME_T2.toInstant()));
        assertEquals(2, entities.size());
        assertThat(entities, CoreMatchers.hasItems(MESSAGE_ID_SEND_ENQUEUED_RECIPIENT));

    }

    private void initialize() {
        oneMessage(MESSAGE_ID_SEND_FAILURE, MessageStatus.SEND_FAILURE, null, Date.from(ZONED_DATE_TIME_T1.toInstant()));
        oneMessage(MESSAGE_ID_SEND_FAILURE_RECIPIENT, MessageStatus.SEND_FAILURE, "finalRecipient", Date.from(ZONED_DATE_TIME_T2.toInstant()));
        oneMessage(MESSAGE_ID_ACKNOWLEDGED, MessageStatus.ACKNOWLEDGED, null, null);
        oneMessage_sendEnqueued(MESSAGE_ID_SEND_ENQUEUED, MessageStatus.SEND_ENQUEUED, null, Date.from(ZONED_DATE_TIME_T1.toInstant()));
        oneMessage_sendEnqueued(MESSAGE_ID_SEND_ENQUEUED_RECIPIENT, MessageStatus.SEND_ENQUEUED, "finalRecipient", Date.from(ZONED_DATE_TIME_T2.toInstant()));
    }

    private void oneMessage(String messageId, MessageStatus sendFailure, String finalRecipient, Date failed) {
        UserMessage entity = new UserMessage();
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setMessageId(messageId);
        entity.setMessageInfo(messageInfo);
        CollaborationInfo value = new CollaborationInfo();
        value.setConversationId(messageId);
        entity.setCollaborationInfo(value);

        UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setMessageStatus(sendFailure);
        userMessageLog.setMessageType(MessageType.USER_MESSAGE);
        userMessageLog.setDeleted(null);
        userMessageLog.setMessageId(messageId);
        userMessageLog.setFailed(failed);

        if (finalRecipient != null) {
            Property property = new Property();
            property.setName("finalRecipient");
            property.setValue(finalRecipient);
            MessageProperties value1 = new MessageProperties();
            value1.getProperty().add(property);
            entity.setMessageProperties(value1);
        }

        em.persist(messageInfo);
        userMessageLogDao.create(userMessageLog);
        userMessageDao.create(entity);
    }

    private void oneMessage_sendEnqueued(String messageId, MessageStatus sendEnqueued, String finalRecipient, Date received) {
        UserMessage entity = new UserMessage();
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setMessageId(messageId);
        entity.setMessageInfo(messageInfo);
        CollaborationInfo value = new CollaborationInfo();
        value.setConversationId(messageId);
        entity.setCollaborationInfo(value);

        UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setMessageStatus(sendEnqueued);
        userMessageLog.setMessageType(MessageType.USER_MESSAGE);
        userMessageLog.setReceived(received);
        userMessageLog.setDeleted(null);
        userMessageLog.setMessageId(messageId);
        userMessageLog.setFailed(null);

        if (finalRecipient != null) {
            Property property = new Property();
            property.setName("finalRecipient");
            property.setValue(finalRecipient);
            MessageProperties value1 = new MessageProperties();
            value1.getProperty().add(property);
            entity.setMessageProperties(value1);
        }

        em.persist(messageInfo);
        userMessageLogDao.create(userMessageLog);
        userMessageDao.create(entity);
    }
}
