package eu.domibus.common.model.audit.mapper;

import eu.domibus.api.audit.AuditLog;
import eu.domibus.common.model.audit.Audit;
import eu.domibus.common.model.audit.AuditId;

/**
 * This decorator class is used to help the AuditMapper extract the AuditId
 * @author Ion Perpegel
 * @since 4.1.4
 */
public abstract class AuditMapperDecorator implements AuditMapper {

    @Override
    public AuditId auditLogToAuditId(AuditLog auditLog) {
        return new AuditId(
                auditLog.getId(),
                auditLog.getRevisionId(),
                auditLog.getAuditTargetName(),
                auditLog.getAction(),
                auditLog.getChanged());
    }

    @Override
    public Audit auditLogToAudit(AuditLog auditLog) {
        Audit audit = new Audit();
        audit.setId(this.auditLogToAuditId(auditLog));
        audit.setUser(auditLog.getUser());
        return audit;
    }
}
