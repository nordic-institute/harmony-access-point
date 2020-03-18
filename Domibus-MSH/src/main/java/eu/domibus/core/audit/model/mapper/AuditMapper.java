package eu.domibus.core.audit.model.mapper;

import eu.domibus.api.audit.AuditLog;
import eu.domibus.core.audit.model.Audit;
import eu.domibus.core.audit.model.AuditId;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Customized mapper for Audit objects
 *
 * @author Ion Perpegel
 * @since 4.1.4
 */
@Mapper(componentModel = "spring")
@DecoratedWith(AuditMapperDecorator.class)
public interface AuditMapper {

    @Mapping(target = "id", ignore = true)
    Audit auditLogToAudit(AuditLog auditLog);

    AuditId auditLogToAuditId(AuditLog auditLog);

}
