package eu.domibus.core.converter;

import eu.domibus.api.audit.AuditLog;
import eu.domibus.core.audit.model.Audit;
import eu.domibus.core.audit.model.AuditId;
import eu.domibus.core.error.ErrorLogEntry;
import eu.domibus.web.rest.ro.AuditResponseRo;
import eu.domibus.web.rest.ro.ErrorLogRO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Date;
import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
@Mapper(componentModel = "spring")
public interface AuditLogCoreMapper {

    List<AuditResponseRo> auditLogListToAuditResponseRoList(List<AuditLog> auditLogList);

    List<AuditLog> auditLogListToAuditList(List<Audit> auditList);

    AuditResponseRo auditLogToAuditResponseRo(AuditLog auditLog);

    AuditLog auditResponseRoToAuditLog(AuditResponseRo auditResponseRo);

    Audit auditLogToAudit(AuditLog auditLog);

    AuditLog auditToAuditLog(Audit audit);

    default AuditId stringToAuditId(String id) {
        return new AuditId(id, null, null, null, new Date());
    }

    ErrorLogRO errorLogEntryToErrorLogRO(ErrorLogEntry errorLogEntry);

    @WithoutAuditAndEntityId
    @Mapping(ignore = true, target = "userMessage")
    @Mapping(target = "mshRole.role", source = "mshRole")
    ErrorLogEntry errorLogROToErrorLogEntry(ErrorLogRO errorLogRO);

    List<ErrorLogRO> errorLogEntryListToErrorLogROList(List<ErrorLogEntry> errorLogEntryList);

    List<ErrorLogEntry> errorLogROListToErrorLogEntryList(List<ErrorLogRO> errorLogROList);

}
