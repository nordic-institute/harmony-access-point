package eu.domibus.core.message;

import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.web.rest.ro.MessageLogResultRO;

import java.util.List;
import java.util.Map;

/**
 * @author Federico Martini
 * @since 3.2
 */
public interface MessagesLogService {
    int countMessages(MessageType messageType, Map<String, Object> filters);

    MessageLogResultRO countAndFindPaged(MessageType messageType, int from, int max, String orderByColumn, boolean asc, Map<String, Object> filters);

    List<MessageLogInfo> findAllInfoCSV(MessageType messageType, int max, String orderByColumn, boolean asc, Map<String, Object> filters);

}
