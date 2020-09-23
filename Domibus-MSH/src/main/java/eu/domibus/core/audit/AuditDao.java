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
    /**
     * Get a list of paginated, filtered and ordered {@link Audit} for the '/audit' page.
     *
     * @param auditTargets to filter by
     * @param actions to filter by
     * @param users to filter by
     * @param from date
     * @param to date
     * @param start of the page
     * @param max items to be returned
     *
     * @return list of {@link Audit}
     * and paginated with {@param start} and {@param max}
     * and filtered by {@param auditTargets}, {@param actions}, {@param users}, {@param from} and {@param to}
     * and ordered by {@link AuditId#getChanged()} and by {@link AuditId#getId()}
     */
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
