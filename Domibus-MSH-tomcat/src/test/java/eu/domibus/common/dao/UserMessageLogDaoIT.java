package eu.domibus.common.dao;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.*;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.MessageDaoTestUtil;
import eu.domibus.core.earchive.EArchiveBatchUserMessage;
import eu.domibus.core.message.MessageLogInfo;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.DATETIME_FORMAT_DEFAULT;
import static eu.domibus.api.model.MessageStatus.*;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Transactional
public class UserMessageLogDaoIT extends AbstractIT {
    public static final String TIMEZONE_ID_AMERICA_LOS_ANGELES = "America/Los_Angeles";
    public static final String MPC = "UserMessageLogDaoITMpc";
    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageLogDaoIT.class);

    private final static String NUMBER_FORMAT_DEFAULT = "%010d";
    public static final String MSG1_ID = "msg1-" + UUID.randomUUID();

    @Autowired
    UserMessageLogDao userMessageLogDao;

    @Autowired
    UserMessageDao userMessageDao;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    MessageDaoTestUtil messageDaoTestUtil;

    private Date before;
    private Date timeT;
    private Date after;
    private Date old;

    private final String deletedNoProperties = randomUUID().toString();
    private final String deletedWithProperties = randomUUID().toString();
    private final String receivedNoProperties = randomUUID().toString();
    private final String receivedWithProperties = randomUUID().toString();
    private final String downloadedNoProperties = randomUUID().toString();
    private final String downloadedWithProperties = randomUUID().toString();
    private final String waitingForRetryNoProperties = randomUUID().toString();
    private final String waitingForRetryWithProperties = randomUUID().toString();
    private final String sendFailureNoProperties = randomUUID().toString();
    private final String sendFailureWithProperties = randomUUID().toString();
    private final String testDate = randomUUID().toString();
    private long maxEntityId;
    private UserMessageLog msg1;

    @Before
    @Transactional
    public void setup() {
        before = dateUtil.fromString("2019-01-01T12:00:00Z");
        timeT = dateUtil.fromString("2020-01-01T12:00:00Z");
        after = dateUtil.fromString("2021-01-01T12:00:00Z");
        old = Date.from(before.toInstant().minusSeconds(60 * 60 * 24)); // one day older than "before"

        msg1 = messageDaoTestUtil.createUserMessageLog(MSG1_ID, timeT);
        messageDaoTestUtil.createUserMessageLog("msg2-"+ UUID.randomUUID(), timeT);
        messageDaoTestUtil.createUserMessageLog("msg3-"+ UUID.randomUUID(), old);

        messageDaoTestUtil.createUserMessageLog(testDate, Date.from(ZonedDateTime.now(ZoneOffset.UTC).toInstant()), MSHRole.RECEIVING, MessageStatus.NOT_FOUND, true, MPC, new Date());

        messageDaoTestUtil.createUserMessageLog(deletedNoProperties, timeT, MSHRole.SENDING, MessageStatus.DELETED, false, MPC, new Date());
        messageDaoTestUtil.createUserMessageLog(receivedNoProperties, timeT, MSHRole.SENDING, RECEIVED, false, MPC, new Date());
        messageDaoTestUtil.createUserMessageLog(downloadedNoProperties, timeT, MSHRole.SENDING, MessageStatus.DOWNLOADED, false, MPC, new Date());
        messageDaoTestUtil.createUserMessageLog(waitingForRetryNoProperties, timeT, MSHRole.SENDING, MessageStatus.WAITING_FOR_RETRY, false, MPC, new Date());
        messageDaoTestUtil.createUserMessageLog(sendFailureNoProperties, timeT, MSHRole.SENDING, MessageStatus.SEND_FAILURE, false, MPC, new Date());

        messageDaoTestUtil.createUserMessageLog(deletedWithProperties, timeT, MSHRole.SENDING, MessageStatus.DELETED, true, MPC, null);
        messageDaoTestUtil.createUserMessageLog(receivedWithProperties, timeT, MSHRole.SENDING, RECEIVED, true, MPC, null);
        messageDaoTestUtil.createUserMessageLog(downloadedWithProperties, timeT, MSHRole.SENDING, MessageStatus.DOWNLOADED, true, MPC, null);
        messageDaoTestUtil.createUserMessageLog(waitingForRetryWithProperties, timeT, MSHRole.SENDING, MessageStatus.WAITING_FOR_RETRY, true, MPC, null);
        messageDaoTestUtil.createUserMessageLog(sendFailureWithProperties, timeT, MSHRole.SENDING, MessageStatus.SEND_FAILURE, true, MPC, null);

        maxEntityId = Long.parseLong(ZonedDateTime
                .now(ZoneOffset.UTC)
                .plusDays(1)
                .format(ofPattern(DATETIME_FORMAT_DEFAULT, Locale.ENGLISH)) + String.format(NUMBER_FORMAT_DEFAULT, 0));

        LOG.putMDC(DomibusLogger.MDC_USER, "test_user");
    }

    @BeforeClass
    public static void setTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone(TIMEZONE_ID_AMERICA_LOS_ANGELES));
    }

    @AfterClass
    public static void resetTimezone() {
        TimeZone.setDefault(null);
    }

    @Test
    public void getSentUserMessagesWithPayloadNotClearedOlderThan_found() {
        List<UserMessageLogDto> downloadedUserMessagesOlderThan =
                userMessageLogDao.getSentUserMessagesOlderThan(dateUtil.fromString(LocalDate.now().getYear() + 2 + "-01-01T12:00:00Z"), MPC, 10, false, false);
        assertEquals(2, downloadedUserMessagesOlderThan.size());
        Assert.assertThat(downloadedUserMessagesOlderThan
                .stream()
                .map(UserMessageLogDto::getMessageId)
                .collect(Collectors.toList()), hasItems(sendFailureNoProperties, sendFailureWithProperties));
        assertEquals(0, getProperties(downloadedUserMessagesOlderThan, sendFailureNoProperties).size());
        assertEquals(2, getProperties(downloadedUserMessagesOlderThan, sendFailureWithProperties).size());
    }

    @Test
    public void getSentUserMessagesWithPayloadNotClearedOlderThan_found_eArchive() {
        List<UserMessageLogDto> downloadedUserMessagesOlderThan =
                userMessageLogDao.getSentUserMessagesOlderThan(dateUtil.fromString(LocalDate.now().getYear() + 2 + "-01-01T12:00:00Z"), MPC, 10, false, true);
        assertEquals(1, downloadedUserMessagesOlderThan.size());
    }

    @Test
    public void getSentUserMessagesWithPayloadNotClearedOlderThan_notFound() {
        List<UserMessageLogDto> deletedUserMessagesOlderThan =
                userMessageLogDao.getSentUserMessagesOlderThan(before, MPC, 10, false, false);
        assertEquals(0, deletedUserMessagesOlderThan.size());
    }

    @Test
    public void getSentUserMessagesOlderThan_found() {
        List<UserMessageLogDto> downloadedUserMessagesOlderThan =
                userMessageLogDao.getSentUserMessagesOlderThan(dateUtil.fromString(LocalDate.now().getYear() + 2 + "-01-01T12:00:00Z"), MPC, 10, true, false);
        assertEquals(2, downloadedUserMessagesOlderThan.size());
        Assert.assertThat(downloadedUserMessagesOlderThan
                .stream()
                .map(UserMessageLogDto::getMessageId)
                .collect(Collectors.toList()), hasItems(sendFailureNoProperties, sendFailureWithProperties));
        assertEquals(0, getProperties(downloadedUserMessagesOlderThan, sendFailureNoProperties).size());
        assertEquals(2, getProperties(downloadedUserMessagesOlderThan, sendFailureWithProperties).size());
    }

    @Test
    public void getSentUserMessagesOlderThan_notFound() {
        List<UserMessageLogDto> deletedUserMessagesOlderThan =
                userMessageLogDao.getSentUserMessagesOlderThan(before, MPC, 10, true, false);
        assertEquals(0, deletedUserMessagesOlderThan.size());
    }

    @Test
    public void getDownloadedUserMessagesOlderThan_found() {
        List<UserMessageLogDto> downloadedUserMessagesOlderThan =
                userMessageLogDao.getDownloadedUserMessagesOlderThan(after, MPC, 10, false);
        assertEquals(2, downloadedUserMessagesOlderThan.size());
        Assert.assertThat(downloadedUserMessagesOlderThan
                .stream()
                .map(UserMessageLogDto::getMessageId)
                .collect(Collectors.toList()), hasItems(downloadedNoProperties, downloadedWithProperties));
        assertEquals(0, getProperties(downloadedUserMessagesOlderThan, downloadedNoProperties).size());
        assertEquals(2, getProperties(downloadedUserMessagesOlderThan, downloadedWithProperties).size());
    }
    @Test
    public void getDownloadedUserMessagesOlderThan_found_eArchive() {
        List<UserMessageLogDto> downloadedUserMessagesOlderThan =
                userMessageLogDao.getDownloadedUserMessagesOlderThan(after, MPC, 10, true);
        assertEquals(1, downloadedUserMessagesOlderThan.size());
    }

    @Test
    public void getDownloadedUserMessagesOlderThan_notFound() {
        List<UserMessageLogDto> deletedUserMessagesOlderThan =
                userMessageLogDao.getDownloadedUserMessagesOlderThan(before, MPC, 10, false);
        assertEquals(0, deletedUserMessagesOlderThan.size());
    }

    @Test
    public void getUndownloadedUserMessagesOlderThan_found() {
        List<UserMessageLogDto> undownloadedUserMessagesOlderThan =
                userMessageLogDao.getUndownloadedUserMessagesOlderThan(after, MPC, 10, false);
        assertEquals(2, undownloadedUserMessagesOlderThan.size());
        Assert.assertThat(undownloadedUserMessagesOlderThan
                .stream()
                .map(UserMessageLogDto::getMessageId)
                .collect(Collectors.toList()), hasItems(receivedNoProperties, receivedWithProperties));
        assertEquals(0, getProperties(undownloadedUserMessagesOlderThan, receivedNoProperties).size());
        assertEquals(2, getProperties(undownloadedUserMessagesOlderThan, receivedWithProperties).size());
    }

    @Test
    public void getUndownloadedUserMessagesOlderThan_found_eArchive() {
        List<UserMessageLogDto> undownloadedUserMessagesOlderThan =
                userMessageLogDao.getUndownloadedUserMessagesOlderThan(after, MPC, 10, true);
        assertEquals(1, undownloadedUserMessagesOlderThan.size());
    }

    @Test
    public void getUndownloadedUserMessagesOlderThan_notFound() {
        List<UserMessageLogDto> deletedUserMessagesOlderThan =
                userMessageLogDao.getUndownloadedUserMessagesOlderThan(before, MPC, 10, false);
        assertEquals(0, deletedUserMessagesOlderThan.size());
    }

    @Test
    public void getDeletedUserMessagesOlderThan_found() {
        List<UserMessageLogDto> deletedUserMessagesOlderThan =
                userMessageLogDao.getDeletedUserMessagesOlderThan(after, MPC, 10, false);
        assertEquals(2, deletedUserMessagesOlderThan.size());
        Assert.assertThat(deletedUserMessagesOlderThan
                .stream()
                .map(UserMessageLogDto::getMessageId)
                .collect(Collectors.toList()), hasItems(deletedNoProperties, deletedWithProperties));
        assertEquals(0, getProperties(deletedUserMessagesOlderThan, deletedNoProperties).size());
        assertEquals(2, getProperties(deletedUserMessagesOlderThan, deletedWithProperties).size());
    }

    @Test
    public void getDeletedUserMessagesOlderThan_found_eArchive() {
        List<UserMessageLogDto> deletedUserMessagesOlderThan =
                userMessageLogDao.getDeletedUserMessagesOlderThan(after, MPC, 10, true);
        assertEquals(1, deletedUserMessagesOlderThan.size());
    }

    @Test
    public void currentAndFutureDateTimesSavedInUtcIrrespectiveOfApplicationTimezone() {
        UserMessageLog retryMessage = userMessageLogDao.findByMessageId(testDate);
        Assert.assertNotNull("Should have found a retry message", retryMessage);

        final Date now = dateUtil.getUtcDate();
        Assert.assertTrue("Should have saved the received date in UTC, irrespective of the application timezone " +
                        "(difference to UTC current date time less than 10 minutes)",
                dateUtil.getDiffMinutesBetweenDates(now, retryMessage.getReceived()) < 10);
    }

    private Map<String, String> getProperties(List<UserMessageLogDto> deletedUserMessagesOlderThan, String deletedWithProperties) {
        return deletedUserMessagesOlderThan.stream()
                .filter(userMessageLogDto -> equalsAnyIgnoreCase(userMessageLogDto.getMessageId(), deletedWithProperties))
                .findAny()
                .map(UserMessageLogDto::getProperties)
                .orElse(null);
    }

    @Test
    public void getDeletedUserMessagesOlderThan_notFound() {
        List<UserMessageLogDto> deletedUserMessagesOlderThan =
                userMessageLogDao.getDeletedUserMessagesOlderThan(before, MPC, 10, false);
        assertEquals(0, deletedUserMessagesOlderThan.size());
    }

    @Test
    @Transactional
    public void testCount() {
        Map<String, Object> filters = Stream.of(new Object[][]{
                {"receivedFrom", before},
                {"receivedTo", after},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));

        long count = userMessageLogDao.countEntries(filters);

        assertEquals(12, count);
    }

    @Test
    @Transactional
    public void testCountWithMoreFilters() {
        Map<String, Object> filters = Stream.of(new Object[][]{
                {"receivedFrom", before},
                {"receivedTo", after},
                {"mshRole", MSHRole.RECEIVING},
                {"messageStatus", RECEIVED},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));

        long count = userMessageLogDao.countEntries(filters);

        assertEquals(2, count);
    }

    @Test
    @Transactional
    public void testFindAllInfoPaged() {
        Map<String, Object> filters = Stream.of(new Object[][]{
                {"receivedFrom", before},
                {"receivedTo", after},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));

        List<MessageLogInfo> messages = userMessageLogDao.findAllInfoPaged(0, 10, "received", true, filters);

        assertEquals(7, messages.size());
    }

    @Test
    @Transactional
    public void testFindLastTestMessageId() {
        UserMessageLog testMessage = messageDaoTestUtil.createTestMessage("msg-test-1");
        String testParty = testMessage.getUserMessage().getPartyInfo().getToParty(); // "domibus-red"

        String messageId = userMessageLogDao.findLastTestMessageId(testParty);
        assertEquals("msg-test-1", messageId);
    }

    @Test
    @Transactional
    public void getMessageInFinalStatus() {
        UserMessageLog testMessage = messageDaoTestUtil.createTestMessageInSend_Failure("msg-test-2");

        UserMessageLog message = userMessageLogDao.findMessageToDeleteNotInFinalStatus("msg-test-2");
        assertEquals("msg-test-2", message.getUserMessage().getMessageId());
    }

    @Test
    @Transactional
    public void findMessagesToDelete() {
        final ZonedDateTime currentDate = ZonedDateTime.now(ZoneOffset.UTC);
        final ZonedDateTime startDate = currentDate.minusDays(1);
        final ZonedDateTime endDate = currentDate.plusDays(1);
        final String finalRecipient = "finalRecipient2";

        List<String> message = userMessageLogDao.findMessagesToDelete(finalRecipient, Date.from(startDate.toInstant()), Date.from(endDate.toInstant()));
        assertEquals(3, message.size());
    }

    @Test
    @Transactional
    public void findFailedMessages() {
        final ZonedDateTime currentDate = ZonedDateTime.now(ZoneOffset.UTC);
        final ZonedDateTime startDate = currentDate.minusDays(1);
        final ZonedDateTime endDate = currentDate.plusDays(1);
        final String finalRecipient = "finalRecipient2";

        List<String> message = userMessageLogDao.findFailedMessages(finalRecipient, Date.from(startDate.toInstant()), Date.from(endDate.toInstant()));
        assertEquals(1, message.size());
    }

    @Test
    @Transactional
    public void findFailedMessagesWithOutDates() {
        List<String> message = userMessageLogDao.findFailedMessages(null, null, null);
        assertEquals(2, message.size());
    }

    @Test
    @Transactional
    public void testFindMessagesForArchiving_oldest() {
        UserMessageLog msg = userMessageLogDao.findByMessageId(downloadedWithProperties);

        List<EArchiveBatchUserMessage> messagesForArchiving = userMessageLogDao.findMessagesForArchivingAsc(0L, maxEntityId, 100);
        assertEquals(5, messagesForArchiving.size());
        assertEquals(Long.valueOf(msg.getEntityId()), messagesForArchiving.get(messagesForArchiving.size() - 1).getUserMessageEntityId());
    }

    @Test
    @Transactional
    public void testFindMessagesForArchiving_rest() {
        UserMessageLog msg1 = userMessageLogDao.findByMessageId(MSG1_ID);

        List<EArchiveBatchUserMessage> messagesForArchiving = userMessageLogDao.findMessagesForArchivingAsc(msg1.getEntityId(), maxEntityId, 20);
        assertEquals(4, messagesForArchiving.size());
    }

    @Test
    @Transactional
    public void updateStatusToArchived() {
        List<UserMessageLog> allUserMessageLogs = messageDaoTestUtil.getAllUserMessageLogs();
        List<Long> resultList = allUserMessageLogs.stream().map(AbstractNoGeneratedPkEntity::getEntityId).collect(Collectors.toList());

        userMessageLogDao.updateArchived(resultList);

        List<UserMessageLog> result = messageDaoTestUtil.getAllUserMessageLogs();

        for (UserMessageLog uml : result) {
            em.refresh(uml);
            Assert.assertNotNull(uml.getArchived());
        }
    }

    @Test
    @Transactional
    public void findRetryMessages() {
        List<Long> retryMessages = userMessageLogDao.findRetryMessages(0, 999999999999999999L);

        assertEquals(2, retryMessages.size());
    }

    @Test
    @Transactional
    public void findMessagesNotFinalAsc() {
        List<EArchiveBatchUserMessage> retryMessages = userMessageLogDao.findMessagesNotFinalAsc(0, 999999999999999999L);

        assertEquals(2, retryMessages.size());
    }

    @Test
    @Transactional
    public void getMessageStatus_messageId() {
        MessageStatus messageStatus = userMessageLogDao.getMessageStatus(msg1.getUserMessage().getMessageId());

        assertEquals(RECEIVED, messageStatus);
    }

    @Test
    @Transactional
    public void getMessageStatus_entityId() {
        MessageStatus messageStatus = userMessageLogDao.getMessageStatus(msg1.getEntityId());

        assertEquals(RECEIVED, messageStatus);
    }

    @Test
    @Transactional
    public void getMessageStatus_messageIdNotFound() {
        MessageStatus messageStatus = userMessageLogDao.getMessageStatus("notfound");

        assertEquals(NOT_FOUND, messageStatus);
    }

    @Test
    @Transactional
    public void getMessageStatus_entityIdNotFound() {
        MessageStatus messageStatus = userMessageLogDao.getMessageStatus(12234567890L);

        assertEquals(NOT_FOUND, messageStatus);
    }

    @Test
    @Transactional
    public void findByMessageIdSafely_notfound() {
        UserMessageLog userMessageLog = userMessageLogDao.findByMessageIdSafely("notFound");

        Assert.assertNull(userMessageLog);
    }

    @Test
    @Transactional
    public void findByMessageIdSafely_ok() {
        UserMessageLog userMessageLog = userMessageLogDao.findByMessageIdSafely(msg1.getUserMessage().getMessageId());

        Assert.assertNotNull(userMessageLog);
    }

    @Test
    @Transactional
    public void findByEntityId() {
        UserMessageLog userMessageLog = userMessageLogDao.findByEntityId(msg1.getEntityId());

        Assert.assertNotNull(userMessageLog);
    }

    @Test
    @Transactional
    public void findByEntityId_notFound() {
        UserMessageLog userMessageLog = userMessageLogDao.findByEntityId(12234567890L);

        Assert.assertNull(userMessageLog);
    }

    @Test
    @Transactional
    public void findByEntityIdSafely() {
        UserMessageLog userMessageLog = userMessageLogDao.findByEntityIdSafely(msg1.getEntityId());

        Assert.assertNotNull(userMessageLog);
    }

    @Test
    @Transactional
    public void findByEntityIdSafely_notFound() {
        UserMessageLog userMessageLog = userMessageLogDao.findByEntityIdSafely(12234567890L);

        Assert.assertNull(userMessageLog);
    }

    @Test
    @Transactional
    public void setMessageStatus_DELETED() {
        userMessageLogDao.setMessageStatus(msg1, DELETED);

        UserMessageLog byEntityId = userMessageLogDao.findByEntityId(msg1.getEntityId());
        assertEquals(DELETED, byEntityId.getMessageStatus());
        Assert.assertNotNull(byEntityId.getDeleted());
        Assert.assertNull(byEntityId.getAcknowledged());
        Assert.assertNull(byEntityId.getDownloaded());
        Assert.assertNull(byEntityId.getFailed());
    }
    @Test
    @Transactional
    public void setMessageStatus_ACKNOWLEDGED() {
        userMessageLogDao.setMessageStatus(msg1, ACKNOWLEDGED);

        UserMessageLog byEntityId = userMessageLogDao.findByEntityId(msg1.getEntityId());
        assertEquals(ACKNOWLEDGED, byEntityId.getMessageStatus());
        Assert.assertNull(byEntityId.getDeleted());
        Assert.assertNotNull(byEntityId.getAcknowledged());
        Assert.assertNull(byEntityId.getDownloaded());
        Assert.assertNull(byEntityId.getFailed());
    }

    @Test
    @Transactional
    public void setMessageStatus_ACKNOWLEDGED_WITH_WARNING() {
        userMessageLogDao.setMessageStatus(msg1, ACKNOWLEDGED_WITH_WARNING);

        UserMessageLog byEntityId = userMessageLogDao.findByEntityId(msg1.getEntityId());
        assertEquals(ACKNOWLEDGED_WITH_WARNING, byEntityId.getMessageStatus());
        Assert.assertNull(byEntityId.getDeleted());
        Assert.assertNotNull(byEntityId.getAcknowledged());
        Assert.assertNull(byEntityId.getDownloaded());
        Assert.assertNull(byEntityId.getFailed());
    }

    @Test
    @Transactional
    public void setMessageStatus_DOWNLOADED() {
        userMessageLogDao.setMessageStatus(msg1, DOWNLOADED);

        UserMessageLog byEntityId = userMessageLogDao.findByEntityId(msg1.getEntityId());
        assertEquals(DOWNLOADED, byEntityId.getMessageStatus());
        Assert.assertNull(byEntityId.getDeleted());
        Assert.assertNull(byEntityId.getAcknowledged());
        Assert.assertNotNull(byEntityId.getDownloaded());
        Assert.assertNull(byEntityId.getFailed());
    }

    @Test
    @Transactional
    public void setMessageStatus_SEND_FAILURE() {
        userMessageLogDao.setMessageStatus(msg1, SEND_FAILURE);

        UserMessageLog byEntityId = userMessageLogDao.findByEntityId(msg1.getEntityId());
        assertEquals(SEND_FAILURE, byEntityId.getMessageStatus());
        Assert.assertNull(byEntityId.getDeleted());
        Assert.assertNull(byEntityId.getAcknowledged());
        Assert.assertNull(byEntityId.getDownloaded());
        Assert.assertNotNull(byEntityId.getFailed());
    }

    @Test
    @Transactional
    public void findBackendForMessageId() {
        String backendForMessageId = userMessageLogDao.findBackendForMessageId(msg1.getUserMessage().getMessageId());
        assertNull(backendForMessageId);
    }

    @Test
    @Transactional
    public void setAsNotified() {
        userMessageLogDao.setAsNotified(msg1);

        UserMessageLog byEntityId = userMessageLogDao.findByEntityId(msg1.getEntityId());
        assertEquals(NotificationStatus.NOTIFIED, byEntityId.getNotificationStatus().getStatus());
    }

    @Test
    public void findAllInfoPaged() {
        List<MessageLogInfo> backend = userMessageLogDao.findAllInfoPaged(0, 5, "BACKEND", true, new HashMap<>());
        assertEquals(5, backend.size());
    }
}
