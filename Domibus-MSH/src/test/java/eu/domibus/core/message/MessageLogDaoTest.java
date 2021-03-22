package eu.domibus.core.message;

import eu.domibus.common.MessageStatus;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sebastian-Ion TINCU
 */
@RunWith(JMockit.class)
public class MessageLogDaoTest {

    @Tested
    private MessageLogDao messageLogDao = new UserMessageLogDao();

    @Injectable
    private UserMessageLogInfoFilter userMessageLogInfoFilter;

    @Injectable
    private CriteriaBuilder criteriaBuilder;

    @Injectable
    private Root<? extends MessageLog> root;

    @Injectable
    private EntityManager entityManager;

    private Map<String, Object> filters = new HashMap<>();

    @Test
    public void testSetMessageStatus_Deleted(@Injectable MessageLog messageLog) {
        // WHEN
        messageLogDao.setMessageStatus(messageLog, MessageStatus.DELETED);

        // THEN
        new Verifications() {{
            messageLog.setMessageStatus(MessageStatus.DELETED);
            messageLog.setDeleted((java.util.Date) any);
            messageLog.setNextAttempt(null);
        }};
    }

    @Test
    public void testSetMessageStatus_Acknowledged(@Injectable MessageLog messageLog) {
        // WHEN
        messageLogDao.setMessageStatus(messageLog, MessageStatus.ACKNOWLEDGED);

        // THEN
        new Verifications() {{
            messageLog.setMessageStatus(MessageStatus.ACKNOWLEDGED);
            messageLog.setNextAttempt(null);
        }};
    }

    @Test
    public void testSetMessageStatus_AcknowledgedWithWarning(@Injectable MessageLog messageLog) {
        // WHEN
        messageLogDao.setMessageStatus(messageLog, MessageStatus.ACKNOWLEDGED_WITH_WARNING);

        // THEN
        new Verifications() {{
            messageLog.setMessageStatus(MessageStatus.ACKNOWLEDGED_WITH_WARNING);
            messageLog.setNextAttempt(null);
        }};
    }

    @Test
    public void testSetMessageStatus_Downloaded(@Injectable MessageLog messageLog) {
        // WHEN
        messageLogDao.setMessageStatus(messageLog, MessageStatus.DOWNLOADED);

        // THEN
        new Verifications() {{
            messageLog.setMessageStatus(MessageStatus.DOWNLOADED);
            messageLog.setDownloaded((java.util.Date) any);
            messageLog.setNextAttempt(null);
        }};
    }

    @Test
    public void testSetMessageStatus_SendFailure(@Injectable MessageLog messageLog) {
        // WHEN
        messageLogDao.setMessageStatus(messageLog, MessageStatus.SEND_FAILURE);

        // THEN
        new Verifications() {{
            messageLog.setMessageStatus(MessageStatus.SEND_FAILURE);
            messageLog.setFailed((java.util.Date) any);
            messageLog.setNextAttempt(null);
        }};
    }

    @Test
    public void testGetMessageStatus_returnsNotFoundStatusForNonExistingMessage(@Injectable TypedQuery<MessageStatus> query) {
        // GIVEN
        new Expectations(messageLogDao) {{
            entityManager.createNamedQuery("UserMessageLog.getMessageStatus", MessageStatus.class);
            result = query;

            query.getSingleResult();
            result = new NoResultException();
        }};

        // WHEN
        MessageStatus result = messageLogDao.getMessageStatus("messageId");

        // THEN
        Assert.assertSame("Should have returned the correct message status for an existing user message log", MessageStatus.NOT_FOUND, result);
    }

    @Test
    public void testGetPredicates_ObjectValue(@Injectable Path<String> path, @Injectable Predicate predicate, @Injectable Object filter) {
        // GIVEN
        filters.put("attribute", filter);

        new Expectations() {{
            root.get("attribute");
            result = path;
            criteriaBuilder.equal(path, filter);
            result = predicate;
        }};

        // WHEN
        List<Predicate> result = messageLogDao.getPredicates(filters, criteriaBuilder, root);

        // THEN
        Assert.assertTrue("Should have returned the correct 'equal' Predicate for an attribute having an Object filter value",
                result.size() == 1 && result.contains(predicate));
    }

    @Test
    public void testGetPredicates_StringValue(@Injectable Path<String> path, @Injectable Predicate predicate) {
        // GIVEN
        filters.put("attribute", "filter");
        new Expectations() {{
            root.get("attribute");
            result = path;
            criteriaBuilder.like(path, "filter");
            result = predicate;
        }};

        // WHEN
        List<Predicate> result = messageLogDao.getPredicates(filters, criteriaBuilder, root);

        // THEN
        Assert.assertTrue("Should have returned the correct 'like' Predicate for an attribute having a non-empty String filter value",
                result.size() == 1 && result.contains(predicate));
    }

    @Test
    public void testGetPredicates_StringValue_doesNothingWhenEmptyKey() {
        // GIVEN
        filters.put("", "filter");

        // WHEN
        List<Predicate> result = messageLogDao.getPredicates(filters, criteriaBuilder, root);

        // THEN
        Assert.assertTrue("Should have ignored any empty attribute names having String filter values", result.isEmpty());
    }

    @Test
    public void testGetPredicates_DateValue_receivedFrom(@Injectable Path<Date> path, @Injectable Predicate predicate) {
        // GIVEN
        filters.put("receivedFrom", Timestamp.valueOf(LocalDateTime.now()));
        new Expectations() {{
            root.<Date>get("received");
            result = path;
            criteriaBuilder.greaterThanOrEqualTo(path, (Timestamp) any);
            result = predicate;
        }};

        // WHEN
        List<Predicate> result = messageLogDao.getPredicates(filters, criteriaBuilder, root);

        // THEN
        Assert.assertTrue("Should have returned the correct 'greaterThanOrEqualTo' Predicate for an attribute having a non-empty Timestamp filter value",
                result.size() == 1 && result.contains(predicate));
    }

    @Test
    public void testGetPredicates_DateValue_receivedTo(@Injectable Path<Date> path, @Injectable Predicate predicate) {
        // GIVEN
        filters.put("receivedTo", Timestamp.valueOf(LocalDateTime.now()));
        new Expectations() {{
            root.<Date>get("received");
            result = path;
            criteriaBuilder.lessThanOrEqualTo(path, (Timestamp) any);
            result = predicate;
        }};

        // WHEN
        List<Predicate> result = messageLogDao.getPredicates(filters, criteriaBuilder, root);

        // THEN
        Assert.assertTrue("Should have returned the correct 'lessThanOrEqualTo' Predicate for an attribute having a non-empty Timestamp filter value",
                result.size() == 1 && result.contains(predicate));
    }

    @Test
    public void testGetPredicates_DateValue_doesNothingWhenEmptyKey() {
        // GIVEN
        filters.put("", new Date(Calendar.getInstance().getTime().getTime()));

        // WHEN
        List<Predicate> result = messageLogDao.getPredicates(filters, criteriaBuilder, root);

        // THEN
        Assert.assertTrue("Should have ignored any empty attribute names having Timestamp filter values", result.isEmpty());
    }
}