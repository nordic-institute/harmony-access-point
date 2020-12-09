package eu.domibus.core.error;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ERRORLOG_CLEANER_BATCH_SIZE;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ERRORLOG_CLEANER_OLDER_DAYS;

/**
 * @author Thomas Dussart
 * @since 3.3
 * <p>
 * Service in charge or persisting errors.
 */
@Service
public class ErrorServiceImpl implements ErrorService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ErrorServiceImpl.class);

    @Autowired
    private ErrorLogDao errorLogDao;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    /**
     * {@inheritDoc}
     *
     */
    //@TODO change the ErrorLogEntry to an ErrorLogEntry from the api. Not possible right now because of MSHRole enumeration not accessible from Domibus-MSH-api
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createErrorLog(ErrorLogEntry errorLogEntry) {
        this.errorLogDao.create(errorLogEntry);
    }


    @Override
    public int deleteErrorLogWithoutMessageIds() {


        int days = domibusPropertyProvider.getIntegerProperty(DOMIBUS_ERRORLOG_CLEANER_OLDER_DAYS);
        int batchSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_ERRORLOG_CLEANER_BATCH_SIZE);
        return errorLogDao.deleteErrorLogsWithoutMessageIdOlderThan(days, batchSize);
    }
}
