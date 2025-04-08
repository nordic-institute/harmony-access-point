package eu.domibus.core.message.retention;

import eu.domibus.api.model.DatabasePartition;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.payload.PartInfoService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DateUtil;
import eu.domibus.api.util.DbSchemaUtil;
import eu.domibus.core.alerts.configuration.common.AlertConfigurationService;
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

import java.util.*;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_ACTIVE;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PARTITIONS_DROP_CHECK_MESSAGES_EARCHIVED;
import static eu.domibus.core.message.retention.PartitionService.PARTITION_NAME_REGEXP;
import static eu.domibus.core.message.retention.PartitionServiceTest.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertFalse;

/**
 * @author idragusa
 * @since 5.0
 */
@RunWith(JMockit.class)
public class MessageRetentionPartitionsServiceTest {

public static final Long NOW_AS_NUMBER = 230702090000000000L;
    public static final long TWO_HOURS = 20000000000L;

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
    DbSchemaUtil dbSchemaUtil;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    DateUtil dateUtil;

    @Injectable
    PartitionService partitionService;

    @Injectable
    AlertConfigurationService alertConfigurationService;

    @Injectable
    PartInfoService partInfoService;

    @Test
    public void testPartitionName() {
        String partitionNameOld = "P23032207";
        String partitionNameNew = "SYS_P12345";

        Assert.assertTrue(partitionNameOld.matches(PARTITION_NAME_REGEXP));
        Assert.assertTrue(partitionNameNew.matches(PARTITION_NAME_REGEXP));
    }

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

            domibusConfigurationService.isMultiTenantAware();
            result = false;

            userMessageDao.findAllPartitions();
            result = Arrays.asList(
                    DB_PARTITION_DEFAULT,
                    DB_PARTITION_MESSAGES_BEFORE_PARTIONING,
                    DB_PARTITION_UNTIL_NOW_MINUS_1H
            );

            partitionService.getPartitionHighValueFromDate(withAny(new Date()));
            result = NOW_AS_NUMBER;
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


    @Test
    public void testGetExpiredPartitionsWithNothingExpired() {
        List<DatabasePartition> partitions = Arrays.asList(
                DB_PARTITION_DEFAULT,
                DB_PARTITION_MESSAGES_BEFORE_PARTIONING
        );
        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = false;

            userMessageDao.findAllPartitions();
            result = partitions;

            partitionService.getNewestNonDefaultPartition(partitions);
            result = DB_PARTITION_MESSAGES_BEFORE_PARTIONING;

            partitionService.getPartitionHighValueFromDate(withAny(new Date()));
            result = NOW_AS_NUMBER - TWO_HOURS;

        }};

        List<DatabasePartition> expiredPartitions = messageRetentionPartitionsService.getExpiredPartitionNames(Arrays.asList(new DatabasePartition("TEST", 120L)));

        assertThat(expiredPartitions, empty());
    }

    @Test
    public void testGetExpiredPartitionsWithNoPartitions() {
        List<DatabasePartition> partitions = Collections.singletonList(
                DB_PARTITION_DEFAULT
        );
        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = false;

            userMessageDao.findAllPartitions();
            result = partitions;

            partitionService.getNewestNonDefaultPartition(partitions);
            result = null;

        }};

        List<DatabasePartition> expiredPartitions = messageRetentionPartitionsService.getExpiredPartitionNames(Arrays.asList(new DatabasePartition("TEST", 120L)));

        assertThat(expiredPartitions, empty());
    }

    @Test
    public void testGetExpiredPartitionsWithOneExpiredPartition() {
        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = false;

            userMessageDao.findAllPartitions();
            result = Arrays.asList(
                    DB_PARTITION_DEFAULT,
                    DB_PARTITION_MESSAGES_BEFORE_PARTIONING,
                    DB_PARTITION_UNTIL_NOW_MINUS_1H,
                    DB_PARTITION_UNTIL_NOW,
                    DB_PARTITION_UNTIL_NOW_PLUS_1H
            );

            partitionService.getPartitionHighValueFromDate(withAny(new Date()));
            result = NOW_AS_NUMBER;
        }};

        List<DatabasePartition> expiredPartitions = messageRetentionPartitionsService.getExpiredPartitionNames(Arrays.asList(new DatabasePartition("TEST", 120L)));

        assertFalse(expiredPartitions.isEmpty());
    }
}
