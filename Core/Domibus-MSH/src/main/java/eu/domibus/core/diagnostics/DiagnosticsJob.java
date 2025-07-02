package eu.domibus.core.diagnostics;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.scheduler.DomibusQuartzJobBean;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Breaz Ionut
 * @since 5.1.9
 */

@DisallowConcurrentExecution //Only one DiagnosticsJob runs at any time
public class DiagnosticsJob extends DomibusQuartzJobBean {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DiagnosticsJob.class);

    @Autowired
    protected AuthUtils authUtils;

    @Autowired
    protected DiagnosticsService diagnosticsService;

    @Override
    protected void executeJob(final JobExecutionContext context, final Domain domain) throws JobExecutionException {
        diagnosticsService.logDiagnosticInfo();
    }

    @Override
    public void setQuartzJobSecurityContext() {
        authUtils.setAuthenticationToSecurityContext("diagnostics_user", "diagnostics_password");
    }
}
