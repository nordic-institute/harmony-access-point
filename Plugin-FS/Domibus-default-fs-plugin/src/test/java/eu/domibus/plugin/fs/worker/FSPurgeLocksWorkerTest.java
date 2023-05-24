package eu.domibus.plugin.fs.worker;

import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomainExtService;
import mockit.Injectable;
import mockit.Tested;
import mockit.VerificationsInOrder;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionContext;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
@RunWith(JMockit.class)
public class FSPurgeLocksWorkerTest {

    @Tested
    private FSPurgeLocksWorker fsPurgeLocksWorker;

    @Injectable
    private FSPurgeLocksService fsPurgeLocksService;

    @Injectable
    private DomainExtService domainExtService;

    @Injectable
    private DomainContextExtService domainContextExtService;

    @Test
    public void testExecuteJob(@Injectable final JobExecutionContext context) throws Exception {
        fsPurgeLocksWorker.executeJob(context, null);

        new VerificationsInOrder(1) {{
            fsPurgeLocksService.purge();
        }};
    }

}