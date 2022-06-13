package eu.domibus.core.message.retention;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.security.functions.AuthenticatedProcedure;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.pmode.ConfigurationDAO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author idragusa
 * @since 5.0
 */
@RunWith(JMockit.class)
public class PartitionWorkerTest {

    @Tested
    PartitionWorker partitionWorker;

    @Injectable
    private ConfigurationDAO configurationDAO;

    @Injectable
    private AuthUtils authUtils;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    PartitionService partitionService;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    DomainService domainService;

    @Injectable
    private DatabaseUtil databaseUtil;

    @Test
    public void executeJob(@Mocked JobExecutionContext context, @Mocked Domain domain) throws JobExecutionException {

        new Expectations() {{
            authUtils.runWithSecurityContext((AuthenticatedProcedure) any, "retention_user", "retention_password");
        }};

        partitionWorker.executeJob(context, domain);

    }
}