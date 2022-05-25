package eu.domibus.core.converter;

import eu.domibus.api.audit.AuditLog;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MSHRoleEntity;
import eu.domibus.core.audit.model.Audit;
import eu.domibus.core.error.ErrorLogEntry;
import eu.domibus.web.rest.ro.AuditResponseRo;
import eu.domibus.web.rest.ro.ErrorLogRO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author François Gautier
 * @since 5.0
 */
public class AuditLogCoreMapperTest extends AbstractMapperTest {

    @Autowired
    private AuditLogCoreMapper auditLogCoreMapper;

    @Test
    public void convertAuditLogAuditResponseRo() {
        AuditLog toConvert = (AuditLog) objectService.createInstance(AuditLog.class);
        final AuditResponseRo converted = auditLogCoreMapper.auditLogToAuditResponseRo(toConvert);
        final AuditLog convertedBack = auditLogCoreMapper.auditResponseRoToAuditLog(converted);

        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void convertAuditLogAudit() {
        AuditLog toConvert = (AuditLog) objectService.createInstance(AuditLog.class);
        final Audit converted = auditLogCoreMapper.auditLogToAudit(toConvert);
        final AuditLog convertedBack = auditLogCoreMapper.auditToAuditLog(converted);

        convertedBack.setRevisionId(toConvert.getRevisionId());
        convertedBack.setAuditTargetName(toConvert.getAuditTargetName());
        convertedBack.setAction(toConvert.getAction());
        convertedBack.setChanged(toConvert.getChanged());

        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void convertErrorLogEntryToErrorLogRO() {
        ErrorLogEntry toConvert = (ErrorLogEntry) objectService.createInstance(ErrorLogEntry.class);
        MSHRoleEntity mshRole = new MSHRoleEntity();
        mshRole.setRole(MSHRole.SENDING);
        toConvert.setMshRole(mshRole);
        final ErrorLogRO converted = auditLogCoreMapper.errorLogEntryToErrorLogRO(toConvert);
        final ErrorLogEntry convertedBack = auditLogCoreMapper.errorLogROToErrorLogEntry(converted);

        convertedBack.setEntityId(toConvert.getEntityId());

        objectService.assertObjects(convertedBack, toConvert);
    }

}