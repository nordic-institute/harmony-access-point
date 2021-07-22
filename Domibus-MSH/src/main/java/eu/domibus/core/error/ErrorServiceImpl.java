package eu.domibus.core.error;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MSHRoleEntity;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.ErrorResultImpl;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.message.dictionary.MshRoleDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
    public void createErrorLog(String messageInErrorId, ErrorCode errorCode, String errorDetail) {
        MSHRoleEntity role = mshRoleDao.findOrCreate(MSHRole.SENDING);
        final ErrorLogEntry errorLogEntry = new ErrorLogEntry(role, messageInErrorId, errorCode, errorDetail);
        errorLogDao.create(errorLogEntry);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void createErrorLog(final EbMS3Exception ebms3Exception) {
        final MSHRoleEntity sendingRole = mshRoleDao.findOrCreate(MSHRole.SENDING);
        ErrorLogEntry errorLogEntry = new ErrorLogEntry(ebms3Exception, sendingRole);
        errorLogDao.create(errorLogEntry);
    }

    @Override
    public void deleteErrorLogWithoutMessageIds() {
        int days = domibusPropertyProvider.getIntegerProperty(DOMIBUS_ERRORLOG_CLEANER_OLDER_DAYS);
        int batchSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_ERRORLOG_CLEANER_BATCH_SIZE);

        errorLogDao.deleteErrorLogsWithoutMessageIdOlderThan(days, batchSize);
    }

    @Override
    public List<? extends ErrorResult> getErrors(String messageId) {
        List<ErrorLogEntry> errorsForMessage = errorLogDao.getErrorsForMessage(messageId);
        return errorsForMessage.stream().map(this::convert).collect(Collectors.toList());
    }

    protected ErrorResultImpl convert(ErrorLogEntry errorLogEntry) {
        ErrorResultImpl result = new ErrorResultImpl();
        result.setErrorCode(errorLogEntry.getErrorCode());
        result.setErrorDetail(errorLogEntry.getErrorDetail());
        result.setMessageInErrorId(errorLogEntry.getMessageInErrorId());
        result.setMshRole(eu.domibus.common.MSHRole.valueOf(errorLogEntry.getMshRole().name()));
        result.setNotified(errorLogEntry.getNotified());
        result.setTimestamp(errorLogEntry.getTimestamp());

        return result;
    }
}
