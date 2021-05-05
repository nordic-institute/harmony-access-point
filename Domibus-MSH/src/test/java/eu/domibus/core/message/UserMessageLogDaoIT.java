package eu.domibus.core.message;

import eu.domibus.api.model.*;
import eu.domibus.api.util.DateUtil;
import eu.domibus.core.property.PropertyConfig;
import eu.domibus.core.scheduler.ReprogrammableService;
import eu.domibus.core.scheduler.SchedulerConfig;
import eu.domibus.core.time.TimeConfig;
import eu.domibus.core.util.UtilConfig;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.test.dao.InMemoryDataBaseConfig;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;
import static org.hamcrest.CoreMatchers.hasItems;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InMemoryDataBaseConfig.class, MessageConfig.class, PropertyConfig.class, UtilConfig.class, SchedulerConfig.class, TimeConfig.class})
@ActiveProfiles("IN_MEMORY_DATABASE")
@Transactional
public class UserMessageLogDaoIT {

    /*private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageLogDaoIT.class);

    public static final String TIMEZONE_ID_AMERICA_LOS_ANGELES = "America/Los_Angeles";

    public static final String MPC = "mpc";

    public static final String BACKEND = "backend";

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private MessageInfoDao messageInfoDao;

    @PersistenceContext(unitName = "domibusJTA")
    protected EntityManager em;

    @Autowired
    private DateUtil dateUtil;

    @Autowired
    private ReprogrammableService reprogrammableService;

    private Date before;
    private Date now;
    private Date after;
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

    @BeforeClass
    public static void setTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone(TIMEZONE_ID_AMERICA_LOS_ANGELES));
    }

    @Before
    public void setUp() {
        before = dateUtil.fromString("2019-01-01T12:00:00Z");
        now = dateUtil.fromString("2020-01-01T12:00:00Z");
        after = dateUtil.fromString("2021-01-01T12:00:00Z");

        createEntities(MessageStatus.DELETED, deletedNoProperties, deletedWithProperties);
        createEntities(MessageStatus.RECEIVED, receivedNoProperties, receivedWithProperties);
        createEntities(MessageStatus.DOWNLOADED, downloadedNoProperties, downloadedWithProperties);
        createEntities(MessageStatus.WAITING_FOR_RETRY, waitingForRetryNoProperties, waitingForRetryWithProperties);
        createEntities(MessageStatus.SEND_FAILURE, sendFailureNoProperties, sendFailureWithProperties);

        LOG.putMDC(DomibusLogger.MDC_USER, "test_user");
    }

    @AfterClass
    public static void resetTimezone() {
        TimeZone.setDefault(null);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void getSentUserMessagesWithPayloadNotClearedOlderThan_found() {
        List<UserMessageLogDto> downloadedUserMessagesOlderThan =
                userMessageLogDao.getSentUserMessagesWithPayloadNotClearedOlderThan(dateUtil.fromString(LocalDate.now().getYear() + 2 + "-01-01T12:00:00Z"), MPC, 10);
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
                userMessageLogDao.getSentUserMessagesWithPayloadNotClearedOlderThan(before, MPC, 10);
        Assert.assertEquals(0, deletedUserMessagesOlderThan.size());
    }

    @SuppressWarnings("ConstantConditions")
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

    @SuppressWarnings("ConstantConditions")
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

    @SuppressWarnings("ConstantConditions")
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

    @SuppressWarnings("ConstantConditions")
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
    public void getDeletedUserMessagesOlderThan_notFound() {
        List<UserMessageLogDto> deletedUserMessagesOlderThan =
                userMessageLogDao.getDeletedUserMessagesOlderThan(before, MPC, 10);
        Assert.assertEquals(0, deletedUserMessagesOlderThan.size());
    }

    @Test
    public void currentAndFutureDateTimesSavedInUtcIrrespectiveOfApplicationTimezone() {
        UserMessageLog retryMessage = userMessageLogDao.findByMessageId(waitingForRetryNoProperties);
        Assert.assertNotNull("Should have found a retry message", retryMessage);

        final Date now = dateUtil.getUtcDate();
        Assert.assertTrue("Should have saved the received date in UTC, irrespective of the application timezone " +
                        "(difference to UTC current date time less than 10 minutes)",
                dateUtil.getDiffMinutesBetweenDates(now, retryMessage.getReceived()) < 10);
        Assert.assertTrue("Should have saved the next attempt date in UTC, irrespective of the application " +
                        "timezone (difference to UTC current date time less than 10 minutes)",
                dateUtil.getDiffMinutesBetweenDates(now, retryMessage.getNextAttempt()) < 10);
        Assert.assertNotNull("Should have correctly saved the next attempt timezone offset considering the server timezone",
                retryMessage.getTimezoneOffset());
        Assert.assertEquals("Should have correctly saved the next attempt timezone ID considering the server timezone",
                "America/Los_Angeles", retryMessage.getTimezoneOffset().getNextAttemptTimezoneId());
        Assert.assertTrue("Should have correctly saved the next attempt timezone offset in seconds considering the server timezone",
                retryMessage.getTimezoneOffset().getNextAttemptOffsetSeconds() != 0);
    }

    private Map<String, String> getProperties(List<UserMessageLogDto> deletedUserMessagesOlderThan, String deletedWithProperties) {
        return deletedUserMessagesOlderThan.stream()
                .filter(userMessageLogDto -> equalsAnyIgnoreCase(userMessageLogDto.getMessageId(), deletedWithProperties))
                .findAny()
                .orElse(null)
                .getProperties();
    }

    @Test
    public void getDeletedUserMessagesOlderThan_notFound() {
        List<UserMessageLogDto> deletedUserMessagesOlderThan =
                userMessageLogDao.getDeletedUserMessagesOlderThan(before, MPC, 10);
        Assert.assertEquals(0, deletedUserMessagesOlderThan.size());
    }

    private void createEntities(MessageStatus status, String noProperties, String withProperties) {
        MessageInfo messageInfo1 = getMessageInfo(noProperties);
        createUserMessageLog(noProperties, status, now, messageInfo1);
        createUserMessageWithProperties(null, messageInfo1, em);

        MessageInfo messageInfo2 = getMessageInfo(withProperties);
        createUserMessageLog(withProperties, status, now, messageInfo2);
        createUserMessageWithProperties(Arrays.asList(
                getProperty("prop1", "value1"),
                getProperty("prop2", "value2")),
                messageInfo2, em);
    }

    private void createUserMessageWithProperties(List<Property> properties, MessageInfo messageInfo, EntityManager em) {
        UserMessage userMessage = new UserMessage();
        userMessage.setMessageInfo(messageInfo);
        CollaborationInfo collaborationInfo = new CollaborationInfo();
        collaborationInfo.setConversationId(randomUUID().toString());
        userMessage.setCollaborationInfo(collaborationInfo);
        if (properties != null) {
            MessageProperties value = new MessageProperties();
            value.getProperty().addAll(properties);
            userMessage.setMessageProperties(value);
        }
        em.persist(userMessage);
    }

    private Property getProperty(String name, String value) {
        Property property = new Property();
        property.setName(name);
        property.setType("type");
        property.setValue(value);
        return property;
    }

    private void createUserMessageLog(String msgId,
                                      MessageStatus messageStatus,
                                      Date date,
                                      MessageInfo messageInfo) {
        UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setMessageInfo(messageInfo);
        userMessageLog.setMessageId(msgId);
        userMessageLog.setMessageStatus(messageStatus);
        userMessageLog.setMpc(UserMessageLogDaoIT.MPC);
        switch (messageStatus) {
            case DELETED:
                userMessageLog.setDeleted(date);
                break;
            case RECEIVED:
                userMessageLog.setReceived(date);
                break;
            case DOWNLOADED:
                userMessageLog.setDownloaded(date);
                break;
            case WAITING_FOR_RETRY:
                Date now = dateUtil.getUtcDate();
                Date nextAttempt_FiveMinutesLater = new Date(now.getTime() + (5 * 60 * 1000));
                reprogrammableService.setRescheduleInfo(userMessageLog, nextAttempt_FiveMinutesLater);
                break;
        }
        userMessageLog.setBackend(UserMessageLogDaoIT.BACKEND);

        userMessageLogDao.create(userMessageLog);
    }

    private MessageInfo getMessageInfo(String msgId) {
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setMessageId(msgId);
        messageInfo.setTimestamp(new Date());
        messageInfoDao.create(messageInfo);
        return messageInfo;
    }*/

}