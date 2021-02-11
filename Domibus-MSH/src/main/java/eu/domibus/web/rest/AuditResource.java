package eu.domibus.web.rest;

import com.google.common.collect.ImmutableMap;
import eu.domibus.api.audit.AuditLog;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DateUtil;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.audit.envers.ModificationType;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.AuditFilterRequestRO;
import eu.domibus.web.rest.ro.AuditResponseRo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Rest entry point to retrieve the audit logs.
 */
@RestController
@RequestMapping(value = "/rest/audit")
@Validated
public class AuditResource extends BaseResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuditResource.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private DomibusCoreMapper coreMapper;

    @Autowired
    private AuditService auditService;

    @Autowired
    DateUtil dateUtil;

    /**
     * Entry point of the Audit rest service to list the system audit logs.
     *
     * @param auditCriteria the audit criteria used to filter the returned list.
     * @return an audit list.
     */
    @GetMapping(value = {"/list"})
    public List<AuditResponseRo> listAudits(@Valid AuditFilterRequestRO auditCriteria) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Audit criteria received:");
            LOG.debug(auditCriteria.toString());
        }

        List<AuditLog> sourceList = auditService.listAudit(
                auditCriteria.getAuditTargetName(),
                changeActionType(auditCriteria.getAction()),
                auditCriteria.getUser(),
                auditCriteria.getFrom(),
                auditCriteria.getTo(),
                auditCriteria.getStart(),
                auditCriteria.getMax());

        List<AuditResponseRo> list = coreMapper.auditLogListToAuditList(sourceList, AuditResponseRo.class);
        list.forEach(entry -> entry.setAction(getActionTypeLabelFromCode(entry.getAction())));
        return list;
    }

    @GetMapping(value = {"/count"})
    public Long countAudits(@Valid AuditFilterRequestRO auditCriteria) {
        return auditService.countAudit(
                auditCriteria.getAuditTargetName(),
                changeActionType(auditCriteria.getAction()),
                auditCriteria.getUser(),
                auditCriteria.getFrom(),
                auditCriteria.getTo());
    }

    @GetMapping(value = {"/targets"})
    public List<String> auditTargets() {
        return auditService.listAuditTarget();
    }


    /**
     * This method returns a CSV file with the contents of Audit table
     *
     * @param auditCriteria same filter criteria as in filter method
     * @return CSV file with the contents of Audit table
     */
    @GetMapping(path = "/csv")
    public ResponseEntity<String> getCsv(@Valid AuditFilterRequestRO auditCriteria) {
        auditCriteria.setStart(0);
        auditCriteria.setMax(getCsvService().getPageSizeForExport());

        final List<AuditResponseRo> entries = listAudits(auditCriteria);
        getCsvService().validateMaxRows(entries.size(), () -> countAudits(auditCriteria));

        return exportToCSV(entries,
                AuditResponseRo.class,
                ImmutableMap.of("AuditTargetName".toUpperCase(), "Table"),
                Arrays.asList("revisionId"),
                "audit");
    }

    /**
     * Action type send from the admin console are different from the one used in the database.
     * Eg: In the admin console the filter for a modified entity is Modified where in the database a modified record
     * has the MOD flag. This method does the translation.
     */
    private Set<String> changeActionType(Set<String> actions) {
        if (actions == null || actions.isEmpty()) {
            return new HashSet<>();
        }
        return actions.stream()
                .map(this::getActionTypesFromLabel)
                .flatMap(Set::stream).collect(Collectors.toSet());
    }

    private Set<String> getActionTypesFromLabel(String label) {
        return Arrays.stream(ModificationType.values()).
                filter(modificationType -> modificationType.getLabel().equals(label)).
                map(Enum::name).
                collect(Collectors.toSet());
    }

    private String getActionTypeLabelFromCode(String code) {
        return Arrays.stream(ModificationType.values()).
                filter(modificationType -> modificationType.toString().equals(code)).
                map(ModificationType::getLabel).
                findFirst().orElse(code);
    }

}
