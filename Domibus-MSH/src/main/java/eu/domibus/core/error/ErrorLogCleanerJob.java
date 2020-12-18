package eu.domibus.core.error;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.core.scheduler.DomibusQuartzJobBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @since 5.0
 * @author Catalin Enache
 */
@DisallowConcurrentExecution
public class ErrorLogCleanerJob extends DomibusQuartzJobBean {

    @Autowired
    private ErrorService errorService;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {
        errorService.deleteErrorLogWithoutMessageIds();
    }
}
