package eu.domibus.core.message.retention;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.pmode.ConfigurationDAO;
import eu.domibus.core.scheduler.DomibusQuartzJobBean;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_RETENTION_WORKER_DELETION_STRATEGY;


/**
 * @author idragusa
 * @since 5.0
 */
@DisallowConcurrentExecution
public class PartitionWorker extends DomibusQuartzJobBean {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(PartitionWorker.class);

    @Autowired
    private ConfigurationDAO configurationDAO;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected PartitionService partitionService;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) {
        LOG.debug("PartitionWorker to be executed");
        authUtils.runWithSecurityContext(this::executeJob, "retention_user", "retention_password");
    }

    protected void executeJob() {
        if (!configurationDAO.configurationExists()) {
            LOG.debug("Missing pMode configuration.");
            return;
        }
        String retentionStrategy = domibusPropertyProvider.getProperty(DOMIBUS_RETENTION_WORKER_DELETION_STRATEGY);
        if(DeletionStrategy.PARTITIONS != DeletionStrategy.valueOf(retentionStrategy)) {
            LOG.trace("Retention strategy is [{}], do not check for future partitions.", retentionStrategy);
            return;
        }
        partitionService.verifyPartitionsInAdvance();
   }
}