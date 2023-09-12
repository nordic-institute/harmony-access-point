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

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DYNAMICDISCOVERY_CERTIFICATE_RETENTION_HOURS;

/**
 * Job responsible for deleting the certificates lookup up dynamically from SMP
 * The certificate is deleted from the truststore if it was not lookup up via the DDC mechanism during a specific period(meaning it was not used)
 *
 * @author Cosmin Baciu
 * @since 5.1.1
 */
@DisallowConcurrentExecution
public class DynamicDiscoveryCertificateJob extends DomibusQuartzJobBean {
    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryCertificateJob.class);

    @Autowired
    private DynamicDiscoveryCertificateService dynamicDiscoveryCertificateService;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {
        Integer numberOfHours = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DYNAMICDISCOVERY_CERTIFICATE_RETENTION_HOURS);
        if (numberOfHours == null) {
            LOG.debug("DynamicDiscoveryCertificateJob will not be executed because the value of the property [{}] is empty", DOMIBUS_DYNAMICDISCOVERY_CERTIFICATE_RETENTION_HOURS);
            return;
        }
        LOG.debug("Executing DynamicDiscoveryCertificateJob with retention in hours [{}]", numberOfHours);
        dynamicDiscoveryCertificateService.deleteDDCCertificatesNotDiscoveredInTheLastPeriod(numberOfHours);
    }
}
