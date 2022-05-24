package eu.domibus.core.message.retention;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DateUtil;
import eu.domibus.core.alerts.configuration.partitions.PartitionsConfigurationManager;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.pmode.provider.PModeProvider;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_ACTIVE;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PARTITIONS_DROP_CHECK_MESSAGES_EARCHIVED;

/**
 * @author idragusa
 * @since 5.0
 */
@RunWith(JMockit.class)
public class MessageRetentionPartitionsServiceTest {

    @Tested
    MessageRetentionPartitionsService messageRetentionPartitionsService;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    PModeProvider pModeProvider;

    @Injectable
    UserMessageDao userMessageDao;

    @Injectable
    UserMessageLogDao userMessageLogDao;

    @Injectable
    EventService eventService;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    @Injectable
    DomainService domainService;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    DateUtil dateUtil;

    @Injectable
    PartitionService partitionService;

    @Injectable
    PartitionsConfigurationManager partitionsConfigurationManager;

    @Test
    public void deleteExpiredMessagesTest() {

        List<String> mpcs = new ArrayList<>();
        mpcs.add("mpc1");
        mpcs.add("mpc2");
        new Expectations() {{
            pModeProvider.getRetentionDownloadedByMpcURI("mpc1");
            result = 1200;

            pModeProvider.getRetentionDownloadedByMpcURI("mpc2");
            result = 1440;

            pModeProvider.getRetentionUndownloadedByMpcURI(anyString);
            result = 1300;

            pModeProvider.getRetentionSentByMpcURI(anyString);
            result = 600;

            pModeProvider.getMpcURIList();
            result = mpcs;
        }};

        messageRetentionPartitionsService.deleteExpiredMessages();

    }

    @Test
    public void verifySafeGuard() {
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_EARCHIVE_ACTIVE);
            result = false;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_PARTITIONS_DROP_CHECK_MESSAGES_EARCHIVED);
            result = false;

        }};

        Assert.assertTrue(messageRetentionPartitionsService.verifyIfAllMessagesAreArchived("mypart"));
    }
}