package eu.domibus.core.message;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.MessageLogResultRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 4.2.1
 */
@Service
public class MessagesLogServiceHelperImpl implements MessagesLogServiceHelper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagesLogServiceHelperImpl.class);

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    public long calculateNumberOfMessages(MessageLogDaoBase dao, Map<String, Object> filters, MessageLogResultRO result) {
        long count;
        boolean isEstimated;
        Integer limit = domibusPropertyProvider.getIntegerProperty("domibus.UI.messageLogs.countLimit");
        if (limit > 0 && dao.countEntriesWithLimit(filters, limit + 1) >= limit) {
            count = limit;
            isEstimated = true;
        } else {
            count = dao.countEntries(filters);
            isEstimated = false;
        }
        LOG.debug("count User Messages Logs [{}]; is estimated [{}]", count, isEstimated);
        result.setEstimatedCount(isEstimated);
        result.setCount(count);
        return count;
    }

}
