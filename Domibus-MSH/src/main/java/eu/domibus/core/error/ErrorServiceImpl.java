package eu.domibus.core.error;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MSHRoleEntity;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.ErrorCode;
import eu.domibus.core.message.MshRoleDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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

    protected ErrorLogDao errorLogDao;
    protected DomibusPropertyProvider domibusPropertyProvider;
    protected MshRoleDao mshRoleDao;

    public ErrorServiceImpl(ErrorLogDao errorLogDao, DomibusPropertyProvider domibusPropertyProvider, MshRoleDao mshRoleDao) {
        this.errorLogDao = errorLogDao;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.mshRoleDao = mshRoleDao;
    }

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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void createErrorLog(MSHRole mshRole, String messageInErrorId, ErrorCode errorCode, String errorDetail) {
        MSHRoleEntity role = mshRoleDao.findOrCreate(MSHRole.SENDING);
        final ErrorLogEntry errorLogEntry = new ErrorLogEntry(role, messageInErrorId, ErrorCode.EBMS_0004, errorDetail);
        errorLogDao.create(errorLogEntry);
    }

    @Override
    public void deleteErrorLogWithoutMessageIds() {
        int days = domibusPropertyProvider.getIntegerProperty(DOMIBUS_ERRORLOG_CLEANER_OLDER_DAYS);
        int batchSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_ERRORLOG_CLEANER_BATCH_SIZE);

        errorLogDao.deleteErrorLogsWithoutMessageIdOlderThan(days, batchSize);
    }
}
