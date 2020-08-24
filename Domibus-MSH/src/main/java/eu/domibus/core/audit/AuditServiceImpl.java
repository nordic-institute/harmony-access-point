package eu.domibus.core.audit;

import eu.domibus.api.audit.AuditLog;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.PartyIdType;
import eu.domibus.core.audit.envers.ModificationType;
import eu.domibus.core.audit.envers.RevisionLogicalName;
import eu.domibus.core.audit.model.JmsMessageAudit;
import eu.domibus.core.audit.model.MessageAudit;
import eu.domibus.core.audit.model.PModeArchiveAudit;
import eu.domibus.core.audit.model.PModeAudit;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.util.AnnotationsUtil;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Thomas Dussart
 * @since 4.0
 * {@inheritDoc}
 * <p>
 * Service in charge of retrieving audit logs, audit targets, etc...
 */
@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private AuditDao auditDao;

    @Autowired
    private DomainCoreConverter domainCoreConverter;

    @Autowired
    private AnnotationsUtil annotationsUtil;

    @Autowired
    private AuthUtils authUtils;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> listAudit(
            final Set<String> auditTargets,
            final Set<String> actions,
            final Set<String> users,
            final Date from,
            final Date to,
            final int start,
            final int max) {
        return domainCoreConverter.convert(
                auditDao.listAudit(
                        auditTargets,
                        actions,
                        users,
                        from,
                        to,
                        start,
                        max), AuditLog.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Long countAudit(final Set<String> auditTargetName,
                           final Set<String> action,
                           final Set<String> user,
                           final Date from,
                           final Date to) {
        return auditDao.countAudit(auditTargetName, action, user, from, to);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable("auditTarget")
    @Transactional(readOnly = true)
    public List<String> listAuditTarget() {
        Set<Class<?>> typesAnnotatedWith = new Reflections("eu.domibus").
                getTypesAnnotatedWith(RevisionLogicalName.class);
        return typesAnnotatedWith.stream().
                filter(aClass -> aClass != Party.class && aClass != PartyIdType.class).
                map(aClass -> annotationsUtil.getValue(aClass, RevisionLogicalName.class)).
                //check if present is needed because the set contains subclasses that do not contain the annotation.
                        filter(Optional::isPresent).
                        map(Optional::get).
                        distinct().
                        sorted().
                        collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void addPModeDownloadedAudit(final String id) {
        auditDao.savePModeAudit(
                new PModeAudit(id,
                        authUtils.getAuthenticatedUser(),
                        new Date(),
                        ModificationType.DOWNLOADED));
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void addPModeArchiveDownloadedAudit(final String id) {
        auditDao.savePModeArchiveAudit(
                new PModeArchiveAudit(id,
                        authUtils.getAuthenticatedUser(),
                        new Date(),
                        ModificationType.DOWNLOADED));
    }


    /**
     * {@inheritDoc}
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void addMessageDownloadedAudit(final String messageId) {
        auditDao.saveMessageAudit(
                new MessageAudit(messageId,
                        authUtils.getAuthenticatedUser(),
                        new Date(),
                        ModificationType.DOWNLOADED));
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void addMessageResentAudit(final String messageId) {
        auditDao.saveMessageAudit(
                new MessageAudit(messageId,
                        authUtils.getAuthenticatedUser(),
                        new Date(),
                        ModificationType.RESENT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addJmsMessageDeletedAudit(
            final String messageId,
            final String fromQueue) {
        saveJmsMessage(messageId, fromQueue, null, ModificationType.DEL);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addJmsMessageMovedAudit(
            final String messageId,
            final String fromQueue, final String toQueue) {
        saveJmsMessage(messageId, fromQueue, toQueue, ModificationType.MOVED);

    }

    private void saveJmsMessage(final String messageId, final String fromQueue, final String toQueue, final ModificationType modificationType) {
        auditDao.saveJmsMessageAudit(
                new JmsMessageAudit(
                        messageId,
                        authUtils.getAuthenticatedUser(),
                        new Date(),
                        modificationType,
                        fromQueue,
                        toQueue));
    }
}
