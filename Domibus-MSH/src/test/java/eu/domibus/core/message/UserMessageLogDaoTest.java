package eu.domibus.core.message;

import com.google.common.collect.Lists;
import eu.domibus.api.message.MessageSubtype;
import eu.domibus.common.MSHRole;
import eu.domibus.core.plugin.notification.NotificationStatus;
import eu.domibus.ebms3.common.model.MessageType;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.domibus.core.message.UserMessageLogDao.STR_MESSAGE_ID;

/**
 * @author Sebastian-Ion TINCU
 */
@RunWith(JMockit.class)
public class UserMessageLogDaoTest {

    @Tested
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    private UserMessageLogInfoFilter userMessageLogInfoFilter;

    @Injectable
    private EntityManager em;

    @Test
    public void testFindRetryMessages(@Injectable TypedQuery<String> query, @Injectable List<String> retryMessages) {
        // GIVEN
        new Expectations() {{
            em.createNamedQuery("UserMessageLog.findRetryMessages", String.class); result = query;
            query.getResultList(); result = retryMessages;
        }};

        // WHEN
        List<String> result = userMessageLogDao.findRetryMessages();

        // THEN
        Assert.assertSame("Should have correctly returned the retry messages", retryMessages, result);
    }

    @Test
    public void testFindRetryMessages_finalRecipient(@Injectable TypedQuery<String> query) {
        // GIVEN
        new Expectations() {{
            em.createNamedQuery("UserMessageLog.findRetryMessages", String.class); result = query;
        }};

        // WHEN
        userMessageLogDao.findRetryMessages();

        // THEN
        new VerificationsInOrder() {{
            query.setParameter("CURRENT_TIMESTAMP", any);
        }};
    }

    @Test
    public void testFindFailedMessages(@Injectable TypedQuery<String> query, @Injectable List<String> failedMessages) {
        // GIVEN
        new Expectations() {{
            em.createQuery(anyString, String.class); result = query;
            query.getResultList(); result = failedMessages;
        }};

        // WHEN
        List<String> result = userMessageLogDao.findFailedMessages("finalRecipient");

        // THEN
        Assert.assertSame("Should have correctly returned the failed messages", failedMessages, result);
    }

    @Test
    public void testFindFailedMessages_finalRecipient(@Injectable TypedQuery<String> query) {
        // GIVEN
        String finalRecipient = "receiver";

        new Expectations() {{
            em.createQuery(withSubstring(" and p.name = 'finalRecipient' and p.value = :FINAL_RECIPIENT"), String.class); result = query;
        }};

        // WHEN
        userMessageLogDao.findFailedMessages(finalRecipient);

        // THEN
        new VerificationsInOrder() {{
            query.setParameter("FINAL_RECIPIENT", finalRecipient);
        }};
    }

    @Test
    public void testFindFailedMessages_failedStartDate(@Injectable TypedQuery<String> query, @Injectable Date failedStartDate) {
        // GIVEN
        new Expectations() {{
            em.createQuery(withSubstring(" and ml.failed >= :START_DATE"), String.class); result = query;
        }};

        // WHEN
        userMessageLogDao.findFailedMessages(null, failedStartDate, null);

        // THEN
        new VerificationsInOrder() {{
            query.setParameter("START_DATE", failedStartDate);
        }};
    }

    @Test
    public void testFindFailedMessages_failedEndDate(@Injectable TypedQuery<String> query, @Injectable Date failedEndDate) {
        // GIVEN
        new Expectations() {{
            em.createQuery(withSubstring(" and ml.failed <= :END_DATE"), String.class); result = query;
        }};

        // WHEN
        userMessageLogDao.findFailedMessages(null, null, failedEndDate);

        // THEN
        new VerificationsInOrder() {{
            query.setParameter("END_DATE", failedEndDate);
        }};
    }

    @Test
    public void testFindByMessageIdSafely(@Injectable UserMessageLog userMessageLog) {
        // GIVEN
        final String messageId = "messageId";
        new Expectations(userMessageLogDao) {{
            userMessageLogDao.findByMessageId(messageId); result = userMessageLog;
        }};

        // WHEN
        UserMessageLog result = userMessageLogDao.findByMessageIdSafely(messageId);

        // THEN
        Assert.assertSame("Should have returned the existing user log message when searching safely by message identifier", userMessageLog, result);
    }

    @Test
    public void testFindByMessageIdSafely_returnsNullWhenNoResultsFound() {
        // GIVEN
        final String messageId = "messageId";
        new Expectations(userMessageLogDao) {{
            userMessageLogDao.findByMessageId(messageId); result = new NoResultException();
        }};

        // WHEN
        UserMessageLog result = userMessageLogDao.findByMessageIdSafely(messageId);

        // THEN
        Assert.assertNull("Should have returned null when no user log messages found when searching safely by message identifier", result);
    }

    @Test
    public void testFindByMessageId(@Injectable TypedQuery<UserMessageLog> query, @Injectable UserMessageLog userMessageLog) {
        // GIVEN
        new Expectations() {{
            em.createNamedQuery("UserMessageLog.findByMessageId", UserMessageLog.class); result = query;
            query.getSingleResult(); result = userMessageLog;
        }};

        // WHEN
        UserMessageLog result = userMessageLogDao.findByMessageId("messageId");

        // THEN
        new VerificationsInOrder() {{
            Assert.assertSame("Should have returned the existing user log message when searching by message identifier", userMessageLog, result);
        }};
    }

    @Test
    public void testFindByMessageId_messageId(@Injectable TypedQuery<UserMessageLog> query) {
        // GIVEN
        final String messageId = "messageId";
        new Expectations() {{
            em.createNamedQuery("UserMessageLog.findByMessageId", UserMessageLog.class); result = query;
        }};

        // WHEN
        userMessageLogDao.findByMessageId(messageId);

        // THEN
        new VerificationsInOrder() {{
            query.setParameter(STR_MESSAGE_ID, messageId);
        }};
    }

    @Test
    public void testFindByMessageIdAndMshRole(@Injectable TypedQuery<UserMessageLog> query, @Injectable UserMessageLog userMessageLog) {
        // GIVEN
        new Expectations() {{
            em.createNamedQuery("UserMessageLog.findByMessageIdAndRole", UserMessageLog.class); result = query;
            query.getSingleResult(); result = userMessageLog;
        }};

        // WHEN
        UserMessageLog result = userMessageLogDao.findByMessageId("messageId", MSHRole.SENDING);

        // THEN
        new VerificationsInOrder() {{
            Assert.assertSame("Should have returned the existing user log message when searching by message identifier and MSH role", userMessageLog, result);
        }};
    }

    @Test
    public void testFindByMessageIdAndMshRole_messageId(@Injectable TypedQuery<UserMessageLog> query) {
        // GIVEN
        final String messageId = "messageId";
        new Expectations() {{
            em.createNamedQuery("UserMessageLog.findByMessageIdAndRole", UserMessageLog.class); result = query;
        }};

        // WHEN
        userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);

        // THEN
        new VerificationsInOrder() {{
            query.setParameter(STR_MESSAGE_ID, messageId);
        }};
    }

    @Test
    public void testFindByMessageIdAndMshRole_MshRole(@Injectable TypedQuery<UserMessageLog> query) {
        // GIVEN
        final String messageId = "messageId";
        new Expectations() {{
            em.createNamedQuery("UserMessageLog.findByMessageIdAndRole", UserMessageLog.class); result = query;
        }};

        // WHEN
        userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);

        // THEN
        new VerificationsInOrder() {{
            query.setParameter("MSH_ROLE", MSHRole.SENDING);
        }};
    }

    @Test
    public void testFindByMessageIdAndMshRole_returnsNullWhenNoResultsFound(@Injectable TypedQuery<UserMessageLog> query) {
        // GIVEN
        new Expectations() {{
            em.createNamedQuery("UserMessageLog.findByMessageIdAndRole", UserMessageLog.class); result = query;
            query.getSingleResult(); result = new NoResultException();
        }};

        // WHEN
        UserMessageLog result = userMessageLogDao.findByMessageId("messageId", MSHRole.SENDING);

        // THEN
        Assert.assertNull("Should have returned null when no user log messages found when searching safely by message identifier and MSH role", result);
    }

    @Test
    public void testCountMessages(@Injectable Map<String, Object> filters,
                                     @Injectable CriteriaBuilder criteriaBuilder,
                                     @Injectable CriteriaQuery<Long> criteriaQuery,
                                     @Injectable Root<UserMessageLog> root,
                                     @Injectable Path<Long> countPath,
                                     @Injectable Predicate predicate,
                                     @Injectable Predicate conjunction,
                                     @Injectable TypedQuery<Long> query) {
        // GIVEN
        new Expectations(userMessageLogDao) {{
            em.getCriteriaBuilder(); result = criteriaBuilder;
            criteriaBuilder.createQuery(Long.class); result = criteriaQuery;
            criteriaQuery.from(UserMessageLog.class); result = root;
            criteriaBuilder.count(root); result = countPath;
            userMessageLogDao.getPredicates(filters, criteriaBuilder, root); result = Lists.newArrayList(predicate);
            criteriaBuilder.and(new Predicate[] { predicate }); result = conjunction;
            em.createQuery(criteriaQuery); result = query;
            query.getSingleResult(); result = 42;
        }};

        // WHEN
        Long result = userMessageLogDao.countEntries(filters);

        // THEN
        new Verifications() {{
            criteriaQuery.select(countPath);
            criteriaQuery.where(conjunction);
            Assert.assertEquals("Should have returned the correct message count", Long.valueOf(42), result);
        }};
    }

    @Test
    public void testFindPaged_Ascending(@Injectable Map<String, Object> filters,
                                        @Injectable CriteriaBuilder criteriaBuilder,
                                        @Injectable CriteriaQuery<UserMessageLog> criteriaQuery,
                                        @Injectable Root<UserMessageLog> root,
                                        @Injectable Path<String> path,
                                        @Injectable Predicate predicate,
                                        @Injectable Predicate conjunction,
                                        @Injectable Order order,
                                        @Injectable TypedQuery<UserMessageLog> query,
                                        @Injectable List<UserMessageLog> userMessages) {
        // GIVEN
        new Expectations(userMessageLogDao) {{
            em.getCriteriaBuilder(); result = criteriaBuilder;
            criteriaBuilder.createQuery(UserMessageLog.class); result = criteriaQuery;
            criteriaQuery.from(UserMessageLog.class); result = root;
            userMessageLogDao.getPredicates(filters, criteriaBuilder, root); result = Lists.newArrayList(predicate);
            criteriaBuilder.and(new Predicate[] { predicate }); result = conjunction;
            root.get("messageId"); result = path;
            criteriaBuilder.asc(path); result = order;
            em.createQuery(criteriaQuery); result = query;
            query.getResultList(); result = userMessages;
        }};

        // WHEN
        List<UserMessageLog> result = userMessageLogDao.findPaged(0, 10, "messageId", true, filters);

        // THEN
        new Verifications() {{
            criteriaQuery.select(root);
            criteriaQuery.where(conjunction);
            criteriaQuery.orderBy(order);
            query.setFirstResult(0);
            query.setMaxResults(10);

            Assert.assertSame("Should have returned the correct page of user messages in ascending order when filters provided", userMessages, result);
        }};
    }

    @Test
    public void testFindPaged_Descending(@Injectable Map<String, Object> filters,
                                        @Injectable CriteriaBuilder criteriaBuilder,
                                        @Injectable CriteriaQuery<UserMessageLog> criteriaQuery,
                                        @Injectable Root<UserMessageLog> root,
                                        @Injectable Path<String> path,
                                        @Injectable Predicate predicate,
                                        @Injectable Predicate conjunction,
                                        @Injectable Order order,
                                        @Injectable TypedQuery<UserMessageLog> query,
                                        @Injectable List<UserMessageLog> userMessages) {
        // GIVEN
        new Expectations(userMessageLogDao) {{
            em.getCriteriaBuilder(); result = criteriaBuilder;
            criteriaBuilder.createQuery(UserMessageLog.class); result = criteriaQuery;
            criteriaQuery.from(UserMessageLog.class); result = root;
            userMessageLogDao.getPredicates(filters, criteriaBuilder, root); result = Lists.newArrayList(predicate);
            criteriaBuilder.and(new Predicate[] { predicate }); result = conjunction;
            root.get("messageId"); result = path;
            criteriaBuilder.desc(path); result = order;
            em.createQuery(criteriaQuery); result = query;
            query.getResultList(); result = userMessages;
        }};

        // WHEN
        List<UserMessageLog> result = userMessageLogDao.findPaged(0, 10, "messageId", false, filters);

        // THEN
        new Verifications() {{
            criteriaQuery.select(root);
            criteriaQuery.where(conjunction);
            criteriaQuery.orderBy(order);
            query.setFirstResult(0);
            query.setMaxResults(10);

            Assert.assertSame("Should have returned the correct page of user messages in descending order when filters provided", userMessages, result);
        }};
    }

    @Test
    public void testGetUndownloadedUserMessagesOlderThan(@Injectable TypedQuery<String> query, @Injectable List<String> list) {
        // GIVEN
        new Expectations() {{
            em.createNamedQuery("UserMessageLog.findUndownloadedUserMessagesOlderThan", String.class); result = query;
            query.getResultList(); result = list;
        }};

        // WHEN
        List<String> result = userMessageLogDao.getUndownloadedUserMessagesOlderThan(null, null, 1);

        // THEN
        Assert.assertSame("Should have returned the user messages found being not downloaded and older than the provided date", list, result);
    }

    @Test
    public void testGetSentUserMessagesOlderThan(@Injectable TypedQuery<String> query, @Injectable List<String> list) {
        // GIVEN
        new Expectations() {{
            em.createNamedQuery("UserMessageLog.findSentUserMessagesOlderThan", String.class); result = query;
            query.getResultList(); result = list;
        }};

        // WHEN
        List<String> result = userMessageLogDao.getSentUserMessagesOlderThan(null, null, 1);

        // THEN
        Assert.assertSame("Should have returned the user messages found being sent and older than the provided date", list, result);
    }

    @Test
    public void testGetUndownloadedUserMessagesOlderThan_returnsEmptyListWhenNoMessagesFound(@Injectable TypedQuery<String> query) {
        // GIVEN
        new Expectations() {{
            em.createNamedQuery("UserMessageLog.findUndownloadedUserMessagesOlderThan", String.class); result = query;
            query.getResultList(); result = new NoResultException();
        }};

        // WHEN
        List<String> result = userMessageLogDao.getUndownloadedUserMessagesOlderThan(null, null, 1);

        // THEN
        Assert.assertTrue("Should have returned an empty list when no messages found being not downloaded and older than the provided date", result.isEmpty());
    }

    @Test
    public void testGetUndownloadedUserMessagesOlderThan_Date(@Injectable Date startDate, @Injectable TypedQuery<String> query) {
        // GIVEN
        new Expectations() {{
            em.createNamedQuery("UserMessageLog.findUndownloadedUserMessagesOlderThan", String.class); result = query;
        }};

        // WHEN
        userMessageLogDao.getUndownloadedUserMessagesOlderThan(startDate, null, 1);

        // THEN
        new Verifications() {{
            query.setParameter("DATE", startDate);
        }};
    }

    @Test
    public void testGetUndownloadedUserMessagesOlderThan_Mpc(@Injectable TypedQuery<String> query) {
        // GIVEN
        new Expectations() {{
            em.createNamedQuery("UserMessageLog.findUndownloadedUserMessagesOlderThan", String.class); result = query;
        }};

        // WHEN
        userMessageLogDao.getUndownloadedUserMessagesOlderThan(null, "mpc", 1);

        // THEN
        new Verifications() {{
            query.setParameter("MPC", "mpc");
        }};
    }

    @Test
    public void testGetUndownloadedUserMessagesOlderThan_ExpiredNotDownloadedMessagesLimit(@Injectable TypedQuery<String> query) {
        // GIVEN
        new Expectations() {{
            em.createNamedQuery("UserMessageLog.findUndownloadedUserMessagesOlderThan", String.class); result = query;
        }};

        // WHEN
        userMessageLogDao.getUndownloadedUserMessagesOlderThan(null, null, 13);

        // THEN
        new Verifications() {{
            query.setMaxResults(13);
        }};
    }






    @Test
    public void testGetDownloadedUserMessagesOlderThan(@Injectable TypedQuery<String> query, @Injectable List<String> list) {
        // GIVEN
        new Expectations() {{
            em.createNamedQuery("UserMessageLog.findDownloadedUserMessagesOlderThan", String.class); result = query;
            query.getResultList(); result = list;
        }};

        // WHEN
        List<String> result = userMessageLogDao.getDownloadedUserMessagesOlderThan(null, null, 1);

        // THEN
        Assert.assertSame("Should have returned the user messages that are not downloaded and older than the provided date", list, result);
    }

    @Test
    public void testGetDownloadedUserMessagesOlderThan_returnsEmptyListWhenNoMessagesFound(@Injectable TypedQuery<String> query) {
        // GIVEN
        new Expectations() {{
            em.createNamedQuery("UserMessageLog.findDownloadedUserMessagesOlderThan", String.class); result = query;
            query.getResultList(); result = new NoResultException();
        }};

        // WHEN
        List<String> result = userMessageLogDao.getDownloadedUserMessagesOlderThan(null, null, 1);

        // THEN
        Assert.assertTrue("Should have returned an empty list when no messages found being downloaded and older than the provided date", result.isEmpty());
    }

    @Test
    public void testGetDownloadedUserMessagesOlderThan_Date(@Injectable Date startDate, @Injectable TypedQuery<String> query) {
        // GIVEN
        new Expectations() {{
            em.createNamedQuery("UserMessageLog.findDownloadedUserMessagesOlderThan", String.class); result = query;
        }};

        // WHEN
        userMessageLogDao.getDownloadedUserMessagesOlderThan(startDate, null, 1);

        // THEN
        new Verifications() {{
            query.setParameter("DATE", startDate);
        }};
    }

    @Test
    public void testGetDownloadedUserMessagesOlderThan_Mpc(@Injectable TypedQuery<String> query) {
        // GIVEN
        new Expectations() {{
            em.createNamedQuery("UserMessageLog.findDownloadedUserMessagesOlderThan", String.class); result = query;
        }};

        // WHEN
        userMessageLogDao.getDownloadedUserMessagesOlderThan(null, "mpc", 1);

        // THEN
        new Verifications() {{
            query.setParameter("MPC", "mpc");
        }};
    }

    @Test
    public void testGetDownloadedUserMessagesOlderThan_ExpiredDownloadedMessagesLimit(@Injectable TypedQuery<String> query) {
        // GIVEN
        new Expectations() {{
            em.createNamedQuery("UserMessageLog.findDownloadedUserMessagesOlderThan", String.class); result = query;
        }};

        // WHEN
        userMessageLogDao.getDownloadedUserMessagesOlderThan(null, null, 13);

        // THEN
        new Verifications() {{
            query.setMaxResults(13);
        }};
    }

    @Test
    public void testFindBackendForMessageId(@Injectable TypedQuery<String> query) {
        // GIVEN
        new Expectations() {{
            em.createNamedQuery("UserMessageLog.findBackendForMessage", String.class); result = query;
            query.getSingleResult(); result = "backend";
        }};

        // WHEN
        String result = userMessageLogDao.findBackendForMessageId("messageId");

        // THEN
        new Verifications() {{
            query.setParameter(STR_MESSAGE_ID, "messageId");
            Assert.assertEquals("Should have returned the correct backend", "backend", result);
        }};
    }

    @Test
    public void testSetAsNotified(@Injectable UserMessageLog userMessageLog) {
        // WHEN
        userMessageLogDao.setAsNotified(userMessageLog);

        // THEN
        new Verifications() {{
            userMessageLog.setNotificationStatus(NotificationStatus.NOTIFIED);
        }};
    }

    @Test
    public void testCountAllInfo(@Injectable TypedQuery<Number> query) {
        // GIVEN
        Map<String, Object> filters = new HashMap<>();
        filters.put("attribute", new Object());

        new Expectations(userMessageLogDao) {{
            userMessageLogInfoFilter.countUserMessageLogQuery(anyBoolean, filters);
            em.createQuery(anyString, Number.class); result = query;
            userMessageLogInfoFilter.applyParameters(query, filters); result = query;
            query.getSingleResult(); result = Integer.valueOf(4);
        }};

        // WHEN
        int result = userMessageLogDao.countAllInfo(true, filters);

        // THEN
        Assert.assertEquals("Should have returned the correct count when filters provided", 4, result);
    }

    @Test
    public void testCountAllInfo_returnsAllWhenNoFilters() {
        // GIVEN
        new Expectations(userMessageLogDao) {{
            userMessageLogDao.countAll(); result = 7;
        }};

        // WHEN
        int result = userMessageLogDao.countAllInfo(true, new HashMap<>());

        // THEN
        Assert.assertEquals("Should have returned the total count when no filters provided", 7, result);
    }

    @Test
    public void testCountAll(@Injectable Query query) {
        // GIVEN
        new Expectations() {{
            em.createNativeQuery("SELECT count(um.ID_PK) FROM  TB_USER_MESSAGE um"); result = query;
            query.getSingleResult(); result = 10;
        }};

        // WHEN
        Integer result = userMessageLogDao.countAll();

        // THEN
        Assert.assertEquals("Should have returned the correct total count", Integer.valueOf(10), result);
    }

    @Test
    public void testFindAllInfoPaged(@Injectable Map<String, Object> filters,
                                     @Injectable TypedQuery<MessageLogInfo> query,
                                     @Injectable List<MessageLogInfo> information) {
        new Expectations() {{
            userMessageLogInfoFilter.filterMessageLogQuery("messageId", true, filters);
            em.createQuery(anyString, MessageLogInfo.class); result = query;
            userMessageLogInfoFilter.applyParameters(query, filters); result = query;
            query.getResultList(); result = information;
        }};

        // WHEN
        List<MessageLogInfo> result = userMessageLogDao.findAllInfoPaged(0, 10, "messageId", true, filters);

        // THEN
        new Verifications() {{
            query.setFirstResult(0);
            query.setMaxResults(10);
            Assert.assertSame("Should have returned the correct page of information in ascending order when filters provided", information, result);
        }};
    }

    @Test
    public void testFindLastUserTestMessageId(@Injectable TypedQuery<MessageLogInfo> query, @Injectable MessageLogInfo messageLogInfo) {
        // GIVEN
        final Map<String, Object> filters = new HashMap<>();
        filters.put("messageSubtype", MessageSubtype.TEST);
        filters.put("mshRole", MSHRole.SENDING);
        filters.put("toPartyId", "party");
        filters.put("messageType", MessageType.USER_MESSAGE);

        new Expectations() {{
            userMessageLogInfoFilter.filterMessageLogQuery("received", false, withEqual(filters));
            em.createQuery(anyString, MessageLogInfo.class); result = query;
            userMessageLogInfoFilter.applyParameters(query, filters); result = query;
            query.getResultList(); result = Lists.newArrayList(messageLogInfo);
            messageLogInfo.getMessageId(); result = "messageId";
        }};

        // WHEN
        String result = userMessageLogDao.findLastTestMessageId("party");

        // THEN
        new Verifications() {{
            query.setFirstResult(0);
            query.setMaxResults(1);
            Assert.assertSame("Should have returned the correct message identifier when the last user test message is found", "messageId", result);
        }};
    }

    @Test
    public void testFindLastUserTestMessageId_returnsNullWhenTheLastTestMessageNotFound(@Injectable TypedQuery<MessageLogInfo> query) {
        // GIVEN
        new Expectations() {{
            userMessageLogInfoFilter.filterMessageLogQuery("received", false, (Map<String, Object>) any);
            em.createQuery(anyString, MessageLogInfo.class); result = query;
            userMessageLogInfoFilter.applyParameters(query, (Map<String, Object>) any); result = query;
            query.getResultList(); result = Lists.newArrayList();
        }};

        // WHEN
        String result = userMessageLogDao.findLastTestMessageId("party");

        // THEN
        new Verifications() {{
            Assert.assertNull("Should have returned null for the message identifier when the last user test message is not found", result);
        }};
    }
}