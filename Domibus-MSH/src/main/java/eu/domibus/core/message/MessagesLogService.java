package eu.domibus.core.message;

import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.web.rest.ro.MessageLogRO;
import eu.domibus.web.rest.ro.MessageLogResultRO;

import java.util.List;
import java.util.Map;

/**
 * @author Federico Martini
 * @since 3.2
 */
public interface MessagesLogService {

    MessageLogResultRO countAndFindPaged(MessageType messageType, int from, int max, String orderByColumn, boolean asc, Map<String, Object> filters);

    List<MessageLogInfo> findAllInfoCSV(MessageType messageType, int max, String orderByColumn, boolean asc, Map<String, Object> filters);

    /**
     * Retrieves the user message log entry given an id; if not found, returns null
     * @param messageId
     * @return the log entry or null
     */
    MessageLogRO findUserMessageById(String messageId);

}
