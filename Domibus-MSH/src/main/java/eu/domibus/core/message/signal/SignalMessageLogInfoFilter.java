package eu.domibus.core.message.signal;

import eu.domibus.core.message.MessageLogInfoFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@Service(value = "signalMessageLogInfoFilter")
public class SignalMessageLogInfoFilter extends MessageLogInfoFilter {

    private static final String CONVERSATION_ID = "conversationId";

    private static final String EMPTY_CONVERSATION_ID = "'',";

    @Override
    protected String getHQLKey(String originalColumn) {
        if (StringUtils.equals(originalColumn, CONVERSATION_ID)) {
            return "";
        } else {
            return super.getHQLKey(originalColumn);
        }
    }

    @Override
    protected StringBuilder filterQuery(String query, String column, boolean asc, Map<String, Object> filters) {
        if (StringUtils.isNotEmpty(String.valueOf(filters.get(CONVERSATION_ID)))) {
            filters.put(CONVERSATION_ID, null);
        }
        return super.filterQuery(query, column, asc, filters);
    }

    @Override
    public String filterMessageLogQuery(String column, boolean asc, Map<String, Object> filters) {
        String query = "select new eu.domibus.core.message.MessageLogInfo(" +
                "signal.signalMessageId," +
                "log.messageStatus.messageStatus," +
                "log.mshRole.role," +
                "log.deleted," +
                "log.received," +
                EMPTY_CONVERSATION_ID +
                " partyFrom.value," +
                " partyTo.value," +
                (isFourCornerModel() ? " propsFrom.value," : "'',") +
                (isFourCornerModel() ? " propsTo.value," : "'',") +
                "signal.refToMessageId," +
                "message.testMessage" +
                ")" + getQueryBody();
        StringBuilder result = filterQuery(query, column, asc, filters);
        return result.toString();
    }

    public String countSignalMessageLogQuery(boolean asc, Map<String, Object> filters) {
        String query = "select count(message.id)" + getQueryBody();
        StringBuilder result = filterQuery(query, null, asc, filters);
        return result.toString();
    }

    private String getQueryBody() {
        return
                " from SignalMessageLog log " +
                        "join log.signalMessage signal " +
                        "join signal.userMessage message " +
                        (isFourCornerModel() ?
                                "left join message.messageProperties propsFrom "  +
                                        "left join message.messageProperties propsTo " : StringUtils.EMPTY) +
                        "left join message.partyInfo.from.partyId partyFrom " +
                        "left join message.partyInfo.to.partyId partyTo " +
                        (isFourCornerModel() ?
                                "where propsFrom.name = 'originalSender' "  +
                                        "and propsTo.name = 'finalRecipient' " : StringUtils.EMPTY);
    }

}
