package eu.domibus.core.error;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.core.scheduler.DomibusQuartzJobBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @since 5.0
 * @author Catalin Enache
 */
public class ErrorLogCleanerJob extends DomibusQuartzJobBean {

    private ErrorService errorService;

    public ErrorLogCleanerJob(ErrorService errorService) {
        this.errorService = errorService;
    }

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {
        errorService.deleteErrorLogWithoutMessageIds();
    }
}
