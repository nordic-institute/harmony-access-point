package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.quartz.DomibusQuartzJobExtBean;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.esig.dss.tsl.service.DomibusTSLValidationJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * Job to launch dss refresh mechanism.
 */
public class DssRefreshWorker extends DomibusQuartzJobExtBean {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DssRefreshWorker.class);

    @Autowired
    private DomibusTSLValidationJob tslValidationJob;

    @Autowired
    private DssExtensionPropertyManager dssExtensionPropertyManager;

    @Override
    protected void executeJob(JobExecutionContext context, DomainDTO domain) throws JobExecutionException {
        LOG.info("Start DSS trusted lists refresh job");
        if (Boolean.parseBoolean(dssExtensionPropertyManager.getKnownPropertyValue(DssExtensionPropertyManager.DSS_FULL_TLS_REFRESH)))
        {
            tslValidationJob.clearRepository();
            LOG.info("DSS trusted lists cleared");
        }
        tslValidationJob.refresh();
        LOG.info("DSS trusted lists refreshed");
    }
}
