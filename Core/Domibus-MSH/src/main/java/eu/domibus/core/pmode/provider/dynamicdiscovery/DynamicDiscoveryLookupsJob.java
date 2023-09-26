package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.scheduler.DomibusQuartzJobBean;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DYNAMICDISCOVERY_CLEAN_RETENTION_HOURS;

/**
 * Job responsible for deleting the dynamic discovery lookups from SMP
 * If all entries containing a party name are expired, the party will be also deleted from the Pmode
 * If all entries containing a certificate cn are expired, the certificate will be also deleted from the truststore
 *
 * @author Cosmin Baciu
 * @since 5.1.1
 */
@DisallowConcurrentExecution
public class DynamicDiscoveryLookupsJob extends DomibusQuartzJobBean {
    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryLookupsJob.class);

    @Autowired
    private DynamicDiscoveryLookupService dynamicDiscoveryLookupService;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {
        Integer numberOfHours = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DYNAMICDISCOVERY_CLEAN_RETENTION_HOURS);
        if (numberOfHours == null) {
            LOG.debug("DynamicDiscoveryCertificateJob will not be executed because the value of the property [{}] is empty", DOMIBUS_DYNAMICDISCOVERY_CLEAN_RETENTION_HOURS);
            return;
        }
        LOG.debug("Executing DynamicDiscoveryCertificateJob with retention in hours [{}]", numberOfHours);
        dynamicDiscoveryLookupService.deleteDDCLookupEntriesNotDiscoveredInTheLastPeriod(numberOfHours);
    }
}
