package eu.domibus.common.dao;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.*;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.JPAConstants;
import eu.domibus.common.MessageDaoTestUtil;
import eu.domibus.core.message.MessageLogInfo;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.DATETIME_FORMAT_DEFAULT;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;
import static org.hamcrest.CoreMatchers.hasItems;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Transactional
public class UserMessageLogDaoIT extends AbstractIT {
    public static final String TIMEZONE_ID_AMERICA_LOS_ANGELES = "America/Los_Angeles";
    public static final String MPC = "UserMessageLogDaoITMpc";
    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageLogDaoIT.class);
    private String NUMBER_FORMAT_DEFAULT = "%010d";

    @Autowired
    UserMessageLogDao userMessageLogDao;

    @Autowired
    UserMessageDao userMessageDao;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    MessageDaoTestUtil messageDaoTestUtil;

    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    protected EntityManager em;

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

    @Before
    @Transactional
    public void setup() {
        before = dateUtil.fromString("2019-01-01T12:00:00Z");
        timeT = dateUtil.fromString("2020-01-01T12:00:00Z");
        after = dateUtil.fromString("2021-01-01T12:00:00Z");
        old = Date.from(before.toInstant().minusSeconds(60 * 60 * 24)); // one day older than "before"

        messageDaoTestUtil.createUserMessageLog("msg1", timeT);
        messageDaoTestUtil.createUserMessageLog("msg2", timeT);
        messageDaoTestUtil.createUserMessageLog("msg3", old);

        messageDaoTestUtil.createUserMessageLog(testDate, Date.from(ZonedDateTime.now(ZoneOffset.UTC).toInstant()), MSHRole.RECEIVING, MessageStatus.NOT_FOUND, true, MPC);

        messageDaoTestUtil.createUserMessageLog(deletedNoProperties, timeT, MSHRole.SENDING, MessageStatus.DELETED, false, MPC);
        messageDaoTestUtil.createUserMessageLog(receivedNoProperties, timeT, MSHRole.SENDING, MessageStatus.RECEIVED, false, MPC);
        messageDaoTestUtil.createUserMessageLog(downloadedNoProperties, timeT, MSHRole.SENDING, MessageStatus.DOWNLOADED, false, MPC);
        messageDaoTestUtil.createUserMessageLog(waitingForRetryNoProperties, timeT, MSHRole.SENDING, MessageStatus.WAITING_FOR_RETRY, false, MPC);
        messageDaoTestUtil.createUserMessageLog(sendFailureNoProperties, timeT, MSHRole.SENDING, MessageStatus.SEND_FAILURE, false, MPC);

        messageDaoTestUtil.createUserMessageLog(deletedWithProperties, timeT, MSHRole.SENDING, MessageStatus.DELETED, true, MPC);
        messageDaoTestUtil.createUserMessageLog(receivedWithProperties, timeT, MSHRole.SENDING, MessageStatus.RECEIVED, true, MPC);
        messageDaoTestUtil.createUserMessageLog(downloadedWithProperties, timeT, MSHRole.SENDING, MessageStatus.DOWNLOADED, true, MPC);
        messageDaoTestUtil.createUserMessageLog(waitingForRetryWithProperties, timeT, MSHRole.SENDING, MessageStatus.WAITING_FOR_RETRY, true, MPC);
        messageDaoTestUtil.createUserMessageLog(sendFailureWithProperties, timeT, MSHRole.SENDING, MessageStatus.SEND_FAILURE, true, MPC);

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
                userMessageLogDao.getSentUserMessagesOlderThan(dateUtil.fromString(LocalDate.now().getYear() + 2 + "-01-01T12:00:00Z"), MPC, 10, false);
        Assert.assertEquals(2, downloadedUserMessagesOlderThan.size());
        Assert.assertThat(downloadedUserMessagesOlderThan
                .stream()
                .map(UserMessageLogDto::getMessageId)
                .collect(Collectors.toList()), hasItems(sendFailureNoProperties, sendFailureWithProperties));
        Assert.assertEquals(0, getProperties(downloadedUserMessagesOlderThan, sendFailureNoProperties).size());
        Assert.assertEquals(2, getProperties(downloadedUserMessagesOlderThan, sendFailureWithProperties).size());
    }

    @Test
    public void getSentUserMessagesWithPayloadNotClearedOlderThan_notFound() {
        List<UserMessageLogDto> deletedUserMessagesOlderThan =
                userMessageLogDao.getSentUserMessagesOlderThan(before, MPC, 10, false);
        Assert.assertEquals(0, deletedUserMessagesOlderThan.size());
    }

    @Test
    public void getSentUserMessagesOlderThan_found() {
        List<UserMessageLogDto> downloadedUserMessagesOlderThan =
                userMessageLogDao.getSentUserMessagesOlderThan(dateUtil.fromString(LocalDate.now().getYear() + 2 + "-01-01T12:00:00Z"), MPC, 10, true);
        Assert.assertEquals(2, downloadedUserMessagesOlderThan.size());
        Assert.assertThat(downloadedUserMessagesOlderThan
                .stream()
                .map(UserMessageLogDto::getMessageId)
                .collect(Collectors.toList()), hasItems(sendFailureNoProperties, sendFailureWithProperties));
        Assert.assertEquals(0, getProperties(downloadedUserMessagesOlderThan, sendFailureNoProperties).size());
        Assert.assertEquals(2, getProperties(downloadedUserMessagesOlderThan, sendFailureWithProperties).size());
    }

    @Test
    public void getSentUserMessagesOlderThan_notFound() {
        List<UserMessageLogDto> deletedUserMessagesOlderThan =
                userMessageLogDao.getSentUserMessagesOlderThan(before, MPC, 10, true);
        Assert.assertEquals(0, deletedUserMessagesOlderThan.size());
    }

    @Test
    public void getDownloadedUserMessagesOlderThan_found() {
        List<UserMessageLogDto> downloadedUserMessagesOlderThan =
                userMessageLogDao.getDownloadedUserMessagesOlderThan(after, MPC, 10);
        Assert.assertEquals(2, downloadedUserMessagesOlderThan.size());
        Assert.assertThat(downloadedUserMessagesOlderThan
                .stream()
                .map(UserMessageLogDto::getMessageId)
                .collect(Collectors.toList()), hasItems(downloadedNoProperties, downloadedWithProperties));
        Assert.assertEquals(0, getProperties(downloadedUserMessagesOlderThan, downloadedNoProperties).size());
        Assert.assertEquals(2, getProperties(downloadedUserMessagesOlderThan, downloadedWithProperties).size());
    }

    @Test
    public void getDownloadedUserMessagesOlderThan_notFound() {
        List<UserMessageLogDto> deletedUserMessagesOlderThan =
                userMessageLogDao.getDownloadedUserMessagesOlderThan(before, MPC, 10);
        Assert.assertEquals(0, deletedUserMessagesOlderThan.size());
    }

    @Test
    public void getUndownloadedUserMessagesOlderThan_found() {
        List<UserMessageLogDto> undownloadedUserMessagesOlderThan =
                userMessageLogDao.getUndownloadedUserMessagesOlderThan(after, MPC, 10);
        Assert.assertEquals(2, undownloadedUserMessagesOlderThan.size());
        Assert.assertThat(undownloadedUserMessagesOlderThan
                .stream()
                .map(UserMessageLogDto::getMessageId)
                .collect(Collectors.toList()), hasItems(receivedNoProperties, receivedWithProperties));
        Assert.assertEquals(0, getProperties(undownloadedUserMessagesOlderThan, receivedNoProperties).size());
        Assert.assertEquals(2, getProperties(undownloadedUserMessagesOlderThan, receivedWithProperties).size());
    }

    @Test
    public void getUndownloadedUserMessagesOlderThan_notFound() {
        List<UserMessageLogDto> deletedUserMessagesOlderThan =
                userMessageLogDao.getUndownloadedUserMessagesOlderThan(before, MPC, 10);
        Assert.assertEquals(0, deletedUserMessagesOlderThan.size());
    }

    @Test
    public void getDeletedUserMessagesOlderThan_found() {
        List<UserMessageLogDto> deletedUserMessagesOlderThan =
                userMessageLogDao.getDeletedUserMessagesOlderThan(after, MPC, 10);
        Assert.assertEquals(2, deletedUserMessagesOlderThan.size());
        Assert.assertThat(deletedUserMessagesOlderThan
                .stream()
                .map(UserMessageLogDto::getMessageId)
                .collect(Collectors.toList()), hasItems(deletedNoProperties, deletedWithProperties));
        Assert.assertEquals(0, getProperties(deletedUserMessagesOlderThan, deletedNoProperties).size());
        Assert.assertEquals(2, getProperties(deletedUserMessagesOlderThan, deletedWithProperties).size());
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
                userMessageLogDao.getDeletedUserMessagesOlderThan(before, MPC, 10);
        Assert.assertEquals(0, deletedUserMessagesOlderThan.size());
    }

    @Test
    @Transactional
    public void testCount() {
        Map<String, Object> filters = Stream.of(new Object[][]{
                {"receivedFrom", before},
                {"receivedTo", after},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));

        long count = userMessageLogDao.countEntries(filters);

        Assert.assertEquals(12, count);
    }

    @Test
    @Transactional
    public void testCountWithMoreFilters() {
        Map<String, Object> filters = Stream.of(new Object[][]{
                {"receivedFrom", before},
                {"receivedTo", after},
                {"mshRole", MSHRole.RECEIVING},
                {"messageStatus", MessageStatus.RECEIVED},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));

        long count = userMessageLogDao.countEntries(filters);

        Assert.assertEquals(2, count);
    }

    @Test
    @Transactional
    public void testFindAllInfoPaged() {
        Map<String, Object> filters = Stream.of(new Object[][]{
                {"receivedFrom", before},
                {"receivedTo", after},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));

        List<MessageLogInfo> messages = userMessageLogDao.findAllInfoPaged(0, 10, "received", true, filters);

        Assert.assertEquals(7, messages.size());
    }

    @Test
    @Transactional
    public void testFindLastTestMessageId() {
        UserMessageLog testMessage = messageDaoTestUtil.createTestMessage("msg-test-1");
        String testParty = testMessage.getUserMessage().getPartyInfo().getToParty(); // "domibus-red"

        String messageId = userMessageLogDao.findLastTestMessageId(testParty);
        Assert.assertEquals("msg-test-1", messageId);
    }

    @Test
    @Transactional
    public void getMessageInFinalStatus() {
        UserMessageLog testMessage = messageDaoTestUtil.createTestMessageInSend_Failure("msg-test-2");

        UserMessageLog message = userMessageLogDao.findMessageToDeleteNotInFinalStatus("msg-test-2");
        Assert.assertEquals("msg-test-2", message.getUserMessage().getMessageId());
    }

    @Test
    @Transactional
    public void findMessagesToDelete() {
        final ZonedDateTime currentDate = ZonedDateTime.now(ZoneOffset.UTC);
        final ZonedDateTime startDate = currentDate.minusDays(1);
        final ZonedDateTime endDate = currentDate.plusDays(1);
        final String finalRecipient = "finalRecipient2";

        List<String> message = userMessageLogDao.findMessagesToDelete(finalRecipient, Date.from(startDate.toInstant()), Date.from(endDate.toInstant()));
        Assert.assertEquals(3, message.size());
    }


    @Test
    @Transactional
    public void testFindMessagesForArchiving_oldest() {
        UserMessageLog msg = userMessageLogDao.findByMessageId(downloadedWithProperties);

        ListUserMessageDto messagesForArchiving = userMessageLogDao.findMessagesForArchivingDesc(0L, maxEntityId, 100);
        Assert.assertEquals(7, messagesForArchiving.getUserMessageDtos().size());
        Assert.assertEquals(msg.getEntityId(), messagesForArchiving.getUserMessageDtos().get(messagesForArchiving.getUserMessageDtos().size() - 1).getEntityId());
    }

    @Test
    @Transactional
    public void testFindMessagesForArchiving_rest() {
        UserMessageLog msg1 = userMessageLogDao.findByMessageId("msg1");

        ListUserMessageDto messagesForArchiving = userMessageLogDao.findMessagesForArchivingDesc(msg1.getEntityId(), maxEntityId, 20);
        Assert.assertEquals(6, messagesForArchiving.getUserMessageDtos().size());
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
}
