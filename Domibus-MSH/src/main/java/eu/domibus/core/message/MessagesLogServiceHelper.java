package eu.domibus.core.message;

import eu.domibus.web.rest.ro.MessageLogResultRO;

import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 4.2.1
 */
public interface MessagesLogServiceHelper {

    long calculateNumberOfMessages(MessageLogDaoBase dao, Map<String, Object> filters, MessageLogResultRO result);

}
