package eu.domibus.core.message;

import java.util.Map;

/**
 * Common denominator interface between original and replicated dao classes (MessageLogDao and UIMessageDao)
 * Used as a narrow interface to be able to have common code; more to be added
 *
 * @author Ion Perpegel
 * @since 4.2.1
 */
public interface MessageLogDaoBase {

    /**
     * Counts the messages from message log tables
     *
     * @param filters it should include messageType always - User or Signal message
     * @return number of messages
     */
    long countEntries(Map<String, Object> filters);

    /**
     * Counts the messages from message log tables but no more than the provided limit
     * Used when there are many records
     *
     * @param filters it should include messageType always - User or Signal message
     * @param limit if positive, the result can be maximum the limit
     * @return number of messages
     */
    boolean hasMoreEntriesThan(Map<String, Object> filters, int limit);
}
