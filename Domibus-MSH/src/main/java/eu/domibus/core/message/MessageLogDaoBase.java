package eu.domibus.core.message;

import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 4.2.1
 */
public interface MessageLogDaoBase {

    long countEntries(Map<String, Object> filters);

    long countEntriesWithLimit(Map<String, Object> filters, int limit);
}
