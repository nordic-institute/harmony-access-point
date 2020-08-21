package eu.domibus.common.services.impl;

import eu.domibus.api.audit.AuditLog;
import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.common.dao.AuditDao;
import eu.domibus.common.model.audit.JmsMessageAudit;
import eu.domibus.common.model.audit.MessageAudit;
import eu.domibus.common.model.audit.PModeAudit;
import eu.domibus.common.model.common.ModificationType;
import eu.domibus.common.model.common.RevisionLogicalName;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.PartyIdType;
import eu.domibus.common.services.AuditService;
import eu.domibus.common.util.AnnotationsUtil;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
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

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuditServiceImpl.class);

    @Autowired
    private AuditDao auditDao;

    @Autowired
    private DomainCoreConverter domainCoreConverter;

    @Autowired
    private AnnotationsUtil annotationsUtil;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private DomainService domainService;

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    @Autowired
    private DomainTaskExecutor domainTaskExecutor;

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
    @Override
    public void addPModeDownloadedAudit(final String messageId) {
        auditDao.savePModeAudit(
                new PModeAudit(messageId,
                        authUtils.getAuthenticatedUser(),
                        new Date(),
                        ModificationType.DOWNLOADED));
    }


    /**
     * {@inheritDoc}
     */
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
            final String fromQueue, String domainCode) {

        handleSaveJMSMessage(messageId, fromQueue, ModificationType.DEL, domainCode);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addJmsMessageMovedAudit(
            final String messageId,
            final String fromQueue, final String toQueue, String domainCode) {

        handleSaveJMSMessage(messageId, fromQueue, ModificationType.MOVED, domainCode);
    }

    protected void handleSaveJMSMessage(String messageId, String fromQueue, ModificationType modificationType, String domainCode) {
        Domain domain = domainService.getDomain(domainCode);
        final String userName = authUtils.getAuthenticatedUser();
        if (domibusConfigurationService.isSingleTenant() || (domibusConfigurationService.isMultiTenantAware() && domain == null)) {
            LOG.debug("Audit for JMS Message=[{}] deleted will be saved on default domain", messageId);
            saveJmsMessage(messageId, fromQueue, null, ModificationType.DEL, userName);
        } else {
            domainTaskExecutor.submit(() -> saveJmsMessage(messageId, fromQueue, null, modificationType, userName), domain);
        }
    }

    protected void saveJmsMessage(final String messageId, final String fromQueue, final String toQueue,
                                final ModificationType modificationType, String userName) {
        auditDao.saveJmsMessageAudit(
                new JmsMessageAudit(
                        messageId,
                        userName,
                        new Date(),
                        modificationType,
                        fromQueue,
                        toQueue));
    }
}
