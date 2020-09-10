package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomainExtService;
import eu.europa.esig.dss.tsl.service.DomibusTSLValidationJob;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author Thomas Dussart
 * @since 4.2
 */
@RunWith(JMockit.class)
public class DssRefreshWorkerTest {

    @Injectable
    protected DomainExtService domainExtService;

    @Injectable
    protected DomainContextExtService domainContextExtService;

    @Injectable
    private DomibusTSLValidationJob tslValidationJob;

    @Injectable
    private DssExtensionPropertyManager dssExtensionPropertyManager;

    @Tested
    private DssRefreshWorker dssRefreshWorker;

    @Test
    public void executeJobRefresh(final @Mocked JobExecutionContext context,final @Mocked  DomainDTO domain) throws JobExecutionException {
        new Expectations(){{
            dssExtensionPropertyManager.getKnownPropertyValue(DssExtensionPropertyManager.DSS_FULL_TLS_REFRESH);
            returns("true","false");
        }};
        dssRefreshWorker.executeJob(context,domain);
        dssRefreshWorker.executeJob(context,domain);
        new Verifications(){{
            tslValidationJob.clearRepository();times=1;
            tslValidationJob.refresh();times=2;
        }};
    }
}