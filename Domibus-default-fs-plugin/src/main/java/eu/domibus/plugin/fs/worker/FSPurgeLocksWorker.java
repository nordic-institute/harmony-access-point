package eu.domibus.plugin.fs.worker;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.quartz.DomibusQuartzJobExtBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Quartz based worker responsible for the periodical execution of the FSPurgeLocksService.
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@DisallowConcurrentExecution
public class FSPurgeLocksWorker extends DomibusQuartzJobExtBean {

    @Autowired
    protected FSPurgeLocksService fsPurgeLocksService;

    @Override
    protected void executeJob(JobExecutionContext context, DomainDTO domain) {
        fsPurgeLocksService.purge();
    }

}
