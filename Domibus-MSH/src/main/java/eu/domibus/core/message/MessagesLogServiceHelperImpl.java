package eu.domibus.core.message;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.MessageLogResultRO;
import org.springframework.stereotype.Service;

import java.util.Map;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_UI_MESSAGE_LOGS_COUNT_LIMIT;

/**
 * @author Ion Perpegel
 * @since 4.2.1
 */
@Service
public class MessagesLogServiceHelperImpl implements MessagesLogServiceHelper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagesLogServiceHelperImpl.class);

    private final DomibusPropertyProvider domibusPropertyProvider;

    public MessagesLogServiceHelperImpl(DomibusPropertyProvider domibusPropertyProvider) {
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    @Override
    public long calculateNumberOfMessages(MessageLogDaoBase dao, Map<String, Object> filters, MessageLogResultRO result) {
        long count;
        boolean isEstimated;
        Integer limit = domibusPropertyProvider.getIntegerProperty(DOMIBUS_UI_MESSAGE_LOGS_COUNT_LIMIT);
        if (limit > 0 && dao.hasMoreEntriesThan(filters, limit)) {
            count = limit;
            isEstimated = true;
        } else {
            count = dao.countEntries(filters);
            isEstimated = false;
        }
        LOG.debug("counting [{}] records: [{}]; is estimated [{}]; limit is:[{}].", dao.getClass().getName(), count, isEstimated, limit);
        result.setEstimatedCount(isEstimated);
        result.setCount(count);
        return count;
    }

}
