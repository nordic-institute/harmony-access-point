package eu.domibus.core.message;

import eu.domibus.web.rest.ro.MessageLogResultRO;

import java.util.Map;

/**
 * Helper service that contains common code between messageLog and uiMessage services;
 * more can be extracted and moved here
 *
 * @author Ion Perpegel
 * @since 4.2.1
 */
public interface MessagesLogServiceHelper {

    /**
     * Calculates the number of messages based on filters and a domibus limit configuration
     *
     * @param dao     common denominator between dao classes
     * @param filters the current filters
     * @param result  needed to set the count and estimate flag
     * @return returns the count whick can be real or estimated, in case the limit is specified and achieved
     */
    long calculateNumberOfMessages(MessageLogDaoBase dao, Map<String, Object> filters, MessageLogResultRO result);

}
