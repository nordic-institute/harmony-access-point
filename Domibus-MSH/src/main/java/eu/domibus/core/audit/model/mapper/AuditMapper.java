package eu.domibus.common.model.audit.mapper;

import eu.domibus.api.audit.AuditLog;
import eu.domibus.common.model.audit.Audit;
import eu.domibus.common.model.audit.AuditId;
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
