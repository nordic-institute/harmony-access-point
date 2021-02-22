package eu.domibus.core.message.retention;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.pmode.provider.PModeProvider;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author idragusa
 * @since 4.2.1
 */
@RunWith(JMockit.class)
public class MessageRetentionStoredProcedureServiceTest {

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected PModeProvider pModeProvider;

    @Injectable
    protected DeletionJobService deletionJobService;

    @Injectable
    protected DomainTaskExecutor domainTaskExecutor;

    @Tested
    MessageRetentionStoredProcedureService messageRetentionService;

    final String mpc1 = "mpc1";
    final String mpc2 = "mpc2";
    final Integer expiredDownloadedMessagesLimit = 10;
    final Integer expiredNotDownloadedMessagesLimit = 20;
    final Integer expiredSentMessagesLimit = 30;
    final Integer expiredPayloadDeletedMessagesLimit = 30;
    final List<String> mpcs = Arrays.asList(new String[]{mpc1, mpc2});

    @Before
    public void init() {

    }

    @Test
    public void testDeletionStrategy() {
            Assert.assertTrue(messageRetentionService.handlesDeletionStrategy(DeletionStrategy.STORED_PROCEDURE.name()));
    }

    @Test
    public void testDeleteExpiredMessages() {
        new Expectations(messageRetentionService) {{
            pModeProvider.getMpcURIList();
            result = mpcs;

            messageRetentionService.getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_DOWNLOADED_MAX_DELETE);
            result = expiredDownloadedMessagesLimit;

            messageRetentionService.getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_NOT_DOWNLOADED_MAX_DELETE);
            result = expiredNotDownloadedMessagesLimit;

            messageRetentionService.getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_SENT_MAX_DELETE);
            result = expiredSentMessagesLimit;
        }};

        messageRetentionService.deleteExpiredMessages();

        new Verifications() {{

        }};
    }

    @Test
    public void testGetRetentionValueWithValidRetentionValue() {
        final String propertyName = "retentionLimitProperty";

        new Expectations(messageRetentionService) {{
            domibusPropertyProvider.getIntegerProperty(propertyName);
            result = 5;
        }};

        final Integer retentionValue = messageRetentionService.getRetentionValue(propertyName);
        Assert.assertEquals(retentionValue, Integer.valueOf(5));
    }
}
