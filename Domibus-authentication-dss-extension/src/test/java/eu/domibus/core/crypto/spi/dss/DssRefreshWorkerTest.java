package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.CommandExtService;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomainExtService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.HashMap;

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
    private CommandExtService commandExtService;;

    @Injectable
    private DssRefreshCommand dssRefreshCommand;

    @Tested
    private DssRefreshWorker dssRefreshWorker;

    @Test
    public void executeJobRefresh(final @Mocked JobExecutionContext context,final @Mocked  DomainDTO domain) throws JobExecutionException {
        dssRefreshWorker.executeJob(context,domain);
        new Verifications(){{
            commandExtService.executeCommand(DssRefreshCommand.COMMAND_NAME,withAny(new HashMap<>()));
            dssRefreshCommand.execute(withAny(new HashMap<>()));
        }};
    }
}