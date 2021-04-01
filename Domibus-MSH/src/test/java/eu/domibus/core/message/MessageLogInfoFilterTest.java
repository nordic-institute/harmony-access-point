package eu.domibus.core.message;

import com.google.common.collect.ImmutableMap;
import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.TypedQuery;
import java.util.*;

import static org.mockito.Mockito.spy;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(JMockit.class)
public class MessageLogInfoFilterTest {

    public static final String QUERY1 = "select * from table where z = 1 and log.notificationStatus = :notificationStatus and partyFrom.value = :fromPartyId and log.sendAttemptsMax = :sendAttemptsMax and propsFrom.value = :originalSender and log.received <= :receivedTo and message.collaborationInfo.conversationId = :conversationId and log.messageId = :messageId and info.refToMessageId = :refToMessageId and log.received = :received and log.sendAttempts = :sendAttempts and propsTo.value = :finalRecipient and log.nextAttempt = :nextAttempt and log.messageStatus = :messageStatus and log.deleted = :deleted and log.messageType = :messageType and log.received >= :receivedFrom and partyTo.value = :toPartyId and log.mshRole = :mshRole order by log.messageStatus";

    @Tested
    MessageLogInfoFilter messageLogInfoFilter;

    @Injectable
    private DomibusPropertyProvider domibusProperties;

    public static HashMap<String, Object> returnFilters() {
        HashMap<String, Object> filters = new HashMap<>();

        filters.put("conversationId", "CONVERSATIONID");
        filters.put("messageId", "MESSAGEID");
        filters.put("mshRole", "MSHROLE");
        filters.put("messageType", "MESSAGETYPE");
        filters.put("messageStatus", "MESSAGESTATUS");
        filters.put("notificationStatus", "NOTIFICATIONSTATUS");
        filters.put("deleted", "DELETED");
        filters.put("received", "RECEIVED");
        filters.put("sendAttempts", "SENDATTEMPTS");
        filters.put("sendAttemptsMax", "SENDATTEMPTSMAX");
        filters.put("nextAttempt", "NEXTATTEMPT");
        filters.put("fromPartyId", "FROMPARTYID");
        filters.put("toPartyId", "TOPARTYID");
        filters.put("refToMessageId", "REFTOMESSAGEID");
        filters.put("originalSender", "ORIGINALSENDER");
        filters.put("finalRecipient", "FINALRECIPIENT");
        filters.put("receivedFrom", new Date());
        filters.put("receivedTo", new Date());

        return filters;
    }


    @Test
    public void testGetHQLKeyMessageStatus() {
        Assert.assertEquals("log.messageStatus", messageLogInfoFilter.getHQLKey("messageStatus"));
    }

    @Test
    public void testGetHQLKeyFromPartyId() {
        Assert.assertEquals("partyFrom.value", messageLogInfoFilter.getHQLKey("fromPartyId"));
    }

    @Test
    public void testFilterQueryDesc() {
        StringBuilder filterQuery = messageLogInfoFilter.filterQuery("select * from table where z = 1", "messageStatus", false, returnFilters());

        String filterQueryString = filterQuery.toString();
        Assert.assertTrue(filterQueryString.contains("log.notificationStatus = :notificationStatus"));
        Assert.assertTrue(filterQueryString.contains("partyFrom.value = :fromPartyId"));
        Assert.assertTrue(filterQueryString.contains("log.sendAttemptsMax = :sendAttemptsMax"));
        Assert.assertTrue(filterQueryString.contains("propsFrom.value = :originalSender"));
        Assert.assertTrue(filterQueryString.contains("log.received <= :receivedTo"));
        Assert.assertTrue(filterQueryString.contains("log.messageId = :messageId"));
        Assert.assertTrue(filterQueryString.contains("info.refToMessageId = :refToMessageId"));
        Assert.assertTrue(filterQueryString.contains("log.received = :received"));
        Assert.assertTrue(filterQueryString.contains("log.sendAttempts = :sendAttempts"));
        Assert.assertTrue(filterQueryString.contains("propsTo.value = :finalRecipient"));
        Assert.assertTrue(filterQueryString.contains("log.nextAttempt = :nextAttempt"));
        Assert.assertTrue(filterQueryString.contains("log.messageStatus = :messageStatus"));
        Assert.assertTrue(filterQueryString.contains("log.deleted = :deleted"));
        Assert.assertTrue(filterQueryString.contains("log.messageType = :messageType"));
        Assert.assertTrue(filterQueryString.contains("log.received >= :receivedFrom"));
        Assert.assertTrue(filterQueryString.contains("partyTo.value = :toPartyId"));
        Assert.assertTrue(filterQueryString.contains("log.mshRole = :mshRole"));

        Assert.assertTrue(filterQueryString.contains("log.messageStatus desc"));
    }

    @Test
    public void testFilterQueryAsc() {
        StringBuilder filterQuery = messageLogInfoFilter.filterQuery("select * from table where z = 1", "messageStatus", true, returnFilters());

        String filterQueryString = filterQuery.toString();
        Assert.assertTrue(filterQueryString.contains("log.notificationStatus = :notificationStatus"));
        Assert.assertTrue(filterQueryString.contains("partyFrom.value = :fromPartyId"));
        Assert.assertTrue(filterQueryString.contains("log.sendAttemptsMax = :sendAttemptsMax"));
        Assert.assertTrue(filterQueryString.contains("propsFrom.value = :originalSender"));
        Assert.assertTrue(filterQueryString.contains("log.received <= :receivedTo"));
        Assert.assertTrue(filterQueryString.contains("log.messageId = :messageId"));
        Assert.assertTrue(filterQueryString.contains("info.refToMessageId = :refToMessageId"));
        Assert.assertTrue(filterQueryString.contains("log.received = :received"));
        Assert.assertTrue(filterQueryString.contains("log.sendAttempts = :sendAttempts"));
        Assert.assertTrue(filterQueryString.contains("propsTo.value = :finalRecipient"));
        Assert.assertTrue(filterQueryString.contains("log.nextAttempt = :nextAttempt"));
        Assert.assertTrue(filterQueryString.contains("log.messageStatus = :messageStatus"));
        Assert.assertTrue(filterQueryString.contains("log.deleted = :deleted"));
        Assert.assertTrue(filterQueryString.contains("log.messageType = :messageType"));
        Assert.assertTrue(filterQueryString.contains("log.received >= :receivedFrom"));
        Assert.assertTrue(filterQueryString.contains("partyTo.value = :toPartyId"));
        Assert.assertTrue(filterQueryString.contains("log.mshRole = :mshRole"));

        Assert.assertTrue(filterQueryString.contains("log.messageStatus asc"));
    }

    @Test
    public void testApplyParameters() {
        TypedQuery<MessageLogInfo> typedQuery = spy(TypedQuery.class);
        TypedQuery<MessageLogInfo> messageLogInfoTypedQuery = messageLogInfoFilter.applyParameters(typedQuery, returnFilters());
    }

    @Test
    public void getCountMessageLogQuery(@Mocked Map<String, Object> filters) {
        String result = messageLogInfoFilter.getCountMessageLogQuery(filters);
        Assert.assertTrue(result.contains("select count"));
    }

    @Test
    public void getMessageLogIdQuery(@Mocked Map<String, Object> filters) {
        String result = messageLogInfoFilter.getMessageLogIdQuery(filters);
        Assert.assertTrue(result.contains("select log.id"));
    }

    @Test
    public void getQuery(@Mocked Map<String, Object> filters) {
        String selectExpression = "selectExpression";
        String countQueryBody = "countQueryBody";
        StringBuilder resultQuery = new StringBuilder("resultQuery");

        new Expectations(messageLogInfoFilter) {{
            messageLogInfoFilter.getCountQueryBody(filters);
            result = countQueryBody;
            messageLogInfoFilter.filterQuery(selectExpression + countQueryBody, null, false, filters);
            result = resultQuery;
        }};

        String result = messageLogInfoFilter.getQuery(filters, selectExpression);
        Assert.assertEquals(resultQuery.toString(), result);
    }

    @Test
    public void getCountQueryBody(@Mocked Map<String, Object> allFilters, @Mocked Map<String, Object> filters) {
        StringBuilder fromQuery = new StringBuilder("fromQuery");
        StringBuilder whereQuery = new StringBuilder("whereQuery");

        new Expectations(messageLogInfoFilter) {{
            messageLogInfoFilter.getNonEmptyParams(allFilters);
            result = filters;
            messageLogInfoFilter.createFromClause(filters);
            result = fromQuery;
            messageLogInfoFilter.createWhereQuery(fromQuery);
            result = whereQuery;
        }};

        String result = messageLogInfoFilter.getCountQueryBody(allFilters);
        Assert.assertEquals("fromQuery where whereQuery", result);
    }

    @Test
    public void createFromClause_MainTableOnly() {
        Map<String, Object> filters = ImmutableMap.of(
                "messageId", "111",
                "mshRole", "AP Role",
                "messageType", "type1");

        String mainTable = "UserMessageLog log ";

        String messageTable = ", UserMessage message left join log.messageInfo info ";
        Map<String, List<String>> mappings = ImmutableMap.of(
                "message", Arrays.asList(messageTable),
                "info", Arrays.asList(messageTable));

        new Expectations(messageLogInfoFilter) {{
            messageLogInfoFilter.createFromMappings();
            result = mappings;
            messageLogInfoFilter.getMainTable();
            this.result = mainTable;
        }};

        StringBuilder result = messageLogInfoFilter.createFromClause(filters);
        Assert.assertTrue(result.toString().contains(mainTable));
        Assert.assertFalse(result.toString().contains(messageTable));
    }

    @Test
    public void createFromClause_MessageTableDirectly() {
        Map<String, Object> filters = ImmutableMap.of(
                "messageId", "111",
                "refToMessageId", "222");

        String mainTable = "UserMessageLog log ";

        String messageTable = ", UserMessage message left join log.messageInfo info ";
        Map<String, List<String>> mappings = ImmutableMap.of(
                "message", Arrays.asList(messageTable),
                "info", Arrays.asList(messageTable));

        new Expectations(messageLogInfoFilter) {{
            messageLogInfoFilter.createFromMappings();
            result = mappings;
            messageLogInfoFilter.getMainTable();
            this.result = mainTable;
        }};

        StringBuilder result = messageLogInfoFilter.createFromClause(filters);
        Assert.assertTrue(result.toString().contains(mainTable));
        Assert.assertTrue(result.toString().contains(messageTable));
    }

    @Test
    public void createFromClause_MessageTableNotDirectly() {
        Map<String, Object> filters = ImmutableMap.of(
                "messageId", "111",
                "fromPartyId", "222");

        String mainTable = "UserMessageLog log ";

        String messageTable = ", UserMessage message left join log.messageInfo info ";
        String partyFromTable = "left join message.partyInfo.from.partyId partyFrom ";
        Map<String, List<String>> mappings = ImmutableMap.of(
                "message", Arrays.asList(messageTable),
                "info", Arrays.asList(messageTable),
                "partyFrom", Arrays.asList(messageTable, partyFromTable));

        new Expectations(messageLogInfoFilter) {{
            messageLogInfoFilter.createFromMappings();
            result = mappings;
            messageLogInfoFilter.getMainTable();
            this.result = mainTable;
        }};

        StringBuilder result = messageLogInfoFilter.createFromClause(filters);
        Assert.assertTrue(result.toString().contains(mainTable));
        Assert.assertTrue(result.toString().contains(messageTable));
        Assert.assertTrue(result.toString().contains(partyFromTable));
    }

    @Test
    public void createWhereQuery_MainTableOnly() {
        StringBuilder fromQuery = new StringBuilder(" from UserMessageLog log ");

        String messageCriteria = "message.messageInfo = info ";
        Map<String, List<String>> mappings = ImmutableMap.of(
                "message", Arrays.asList(messageCriteria),
                "propsFrom", Arrays.asList(messageCriteria, "and propsFrom.name = 'originalSender' "));

        new Expectations(messageLogInfoFilter) {{
            messageLogInfoFilter.createWhereMappings();
            result = mappings;
        }};

        StringBuilder result = messageLogInfoFilter.createWhereQuery(fromQuery);
        Assert.assertTrue(StringUtils.isEmpty(result.toString()));
    }

    @Test
    public void createWhereQuery_MessageTableDirectly() {
        StringBuilder fromQuery = new StringBuilder(" from UserMessageLog log , UserMessage message left join log.messageInfo info ");

        String messageCriteria = "message.messageInfo = info ";
        Map<String, List<String>> mappings = ImmutableMap.of(
                "message", Arrays.asList(messageCriteria),
                "propsFrom", Arrays.asList(messageCriteria, "and propsFrom.name = 'originalSender' "));

        new Expectations(messageLogInfoFilter) {{
            messageLogInfoFilter.createWhereMappings();
            result = mappings;
        }};

        StringBuilder result = messageLogInfoFilter.createWhereQuery(fromQuery);
        Assert.assertTrue(result.toString().contains(messageCriteria));
    }

    @Test
    public void createWhereQuery_MessageTableNotDirectly() {
        StringBuilder fromQuery = new StringBuilder(" from UserMessageLog log , UserMessage message left join log.messageInfo info left join message.messageProperties.property propsFrom ");

        String messageCriteria = "message.messageInfo = info ";
        String propsCriteria = "and propsFrom.name = 'originalSender' ";
        Map<String, List<String>> mappings = ImmutableMap.of(
                "message", Arrays.asList(messageCriteria),
                "propsFrom", Arrays.asList(messageCriteria, propsCriteria));

        new Expectations(messageLogInfoFilter) {{
            messageLogInfoFilter.createWhereMappings();
            result = mappings;
        }};

        StringBuilder result = messageLogInfoFilter.createWhereQuery(fromQuery);
        Assert.assertTrue(result.toString().contains(messageCriteria));
        Assert.assertTrue(result.toString().contains(propsCriteria));

    }
}
