package eu.domibus.core.message;

import eu.domibus.web.rest.ro.MessageLogResultRO;

import java.util.Map;

/**
 * Helper service that contains common code between messageLog and uiMessage artefacts; more can be extracted and moved here
 *
 * @author Ion Perpegel
 * @since 4.2.1
 */
public interface MessagesLogServiceHelper {

    /**
     * Calculates the number of messages based on filters and a domibus configuration (real or estimated count)
     *
     * @param dao common denominator between dao classes
     * @param filters the current filters
     * @param result needed to set the count and estimate flag
     * @return returns the count
     */
    long calculateNumberOfMessages(MessageLogDaoBase dao, Map<String, Object> filters, MessageLogResultRO result);

}
