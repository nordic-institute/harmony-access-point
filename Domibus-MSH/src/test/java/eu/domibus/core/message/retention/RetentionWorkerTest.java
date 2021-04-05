package eu.domibus.core.message.retention;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.pmode.ConfigurationDAO;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.ArrayList;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_RETENTION_WORKER_DELETION_STRATEGY;

/**
 * @author Soumya Chandran
 * @since 5.0
 */
@RunWith(JMockit.class)
public class RetentionWorkerTest {

    @Tested
    RetentionWorker retentionWorker;

    @Injectable
    protected List<MessageRetentionService> messageRetentionServices;

    @Injectable
    private ConfigurationDAO configurationDAO;

    @Injectable
    private AuthUtils authUtils;

    @Injectable
    private DomainService domainService;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private DatabaseUtil databaseUtil;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Test
    public void executeJob(@Mocked MessageRetentionDefaultService messageRetentionDefaultService, @Mocked MessageRetentionStoredProcedureService messageRetentionStoredProcedureService, @Mocked JobExecutionContext context, @Mocked Domain domain) throws JobExecutionException {

        String strategy = "STORED_PROCEDURE";
        new Expectations() {{
            configurationDAO.configurationExists();
            result = true;
            domibusPropertyProvider.getProperty(DOMIBUS_RETENTION_WORKER_DELETION_STRATEGY);
            result = strategy;
            messageRetentionDefaultService.handlesDeletionStrategy(strategy);
            result = false;
            messageRetentionStoredProcedureService.handlesDeletionStrategy(strategy);
            result = true;
        }};

        List<MessageRetentionService> listMRS = new ArrayList<>();
        listMRS.add(messageRetentionDefaultService);
        listMRS.add(messageRetentionStoredProcedureService);

        Deencapsulation.setField(retentionWorker, "messageRetentionServices", listMRS);
        retentionWorker.executeJob(context, domain);

        new FullVerifications() {{
            messageRetentionStoredProcedureService.deleteExpiredMessages();
        }};
    }

    @Test
    public void setQuartzJobSecurityContext() {

        retentionWorker.setQuartzJobSecurityContext();

        new FullVerifications() {{
            authUtils.setAuthenticationToSecurityContext("retention_user", "retention_password");
        }};
    }
}