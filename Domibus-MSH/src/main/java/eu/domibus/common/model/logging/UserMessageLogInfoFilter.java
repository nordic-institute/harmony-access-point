package eu.domibus.common.model.logging;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@Service(value = "userMessageLogInfoFilter")
public class UserMessageLogInfoFilter extends MessageLogInfoFilter {

    public String filterUserMessageLogQuery(String column, boolean asc, Map<String, Object> filters) {
        String query = "select log " + getQueryBody(true);
        StringBuilder result = filterQuery(query, column, asc, filters);
        return result.toString();
    }

    public String countUserMessageLogQuery(boolean asc, Map<String, Object> filters) {
        String query = "select count(message.id)" + getQueryBody(false);

        StringBuilder result = filterQuery(query, null, asc, filters);
        return result.toString();
    }

    /**
     * Constructs the query body based on different conditions
     *
     * @return String query body
     */
    private String getQueryBody(boolean fetch) {
        return
                " from  UserMessageLog log join " +
                        (fetch ? "fetch " : StringUtils.EMPTY) +
                        " log.userMessage message " +
                        " where 1=1 "//TODO fix me
                ;

    }


}
