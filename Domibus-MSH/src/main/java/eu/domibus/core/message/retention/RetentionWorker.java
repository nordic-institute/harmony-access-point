package eu.domibus.core.message.retention;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.pmode.ConfigurationDAO;
import eu.domibus.core.scheduler.DomibusQuartzJobBean;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_RETENTION_WORKER_DELETION_STRATEGY;


/**
 * @author Christian Koch, Stefan Mueller
 */
@DisallowConcurrentExecution
public class RetentionWorker extends DomibusQuartzJobBean {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RetentionWorker.class);

    @Autowired
    protected List<MessageRetentionService> messageRetentionServices;

    @Autowired
    private ConfigurationDAO configurationDAO;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) {
        LOG.debug("RetentionWorker executed");
        authUtils.runWithSecurityContext(this::executeJob, "retention_user", "retention_password");
    }

    protected void executeJob() {
        if (!configurationDAO.configurationExists()) {
            LOG.debug("Missing pMode configuration.");
            return;
        }

        String deletionStrategy = domibusPropertyProvider.getProperty(DOMIBUS_RETENTION_WORKER_DELETION_STRATEGY);
        LOG.debug("Deletion strategy is [{}]", deletionStrategy);
        messageRetentionServices.stream()
                .filter(messageRetentionService -> messageRetentionService.handlesDeletionStrategy(deletionStrategy))
                .forEach(messageRetentionService -> messageRetentionService.deleteExpiredMessages());
    }
}
