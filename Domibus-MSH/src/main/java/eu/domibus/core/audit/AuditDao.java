package eu.domibus.core.audit;

import eu.domibus.core.audit.model.*;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public interface AuditDao {

    List<Audit> listAudit(Set<String> auditTargets,
                          Set<String> actions,
                          Set<String> users,
                          Date from,
                          Date to,
                          int start,
                          int max);

    Long countAudit(Set<String> auditTargets,
                    Set<String> actions,
                    Set<String> users,
                    Date from,
                    Date to);

    void saveMessageAudit(MessageAudit messageAudit);

    void savePModeAudit(PModeAudit pmodeAudit);

    void savePModeArchiveAudit(PModeArchiveAudit pmodeArchiveAudit);

    void saveJmsMessageAudit(JmsMessageAudit jmsMessageAudit);
}
