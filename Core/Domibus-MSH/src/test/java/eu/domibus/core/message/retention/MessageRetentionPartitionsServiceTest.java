package eu.domibus.core.message.retention;

import eu.domibus.api.model.DatabasePartition;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DateUtil;
//import eu.domibus.core.alerts.configuration.partitions.PartitionsConfigurationManager;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_ACTIVE;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PARTITIONS_DROP_CHECK_MESSAGES_EARCHIVED;
import static eu.domibus.core.message.retention.MessageRetentionPartitionsService.DEFAULT_PARTITION;
import static eu.domibus.core.message.retention.MessageRetentionPartitionsService.PARTITION_NAME_REGEXP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;

/**
 * @author idragusa
 * @since 5.0
 */
@RunWith(JMockit.class)
public class MessageRetentionPartitionsServiceTest {

    public static final DatabasePartition DB_PARTITION_DEFAULT = new DatabasePartition(DEFAULT_PARTITION, 220000000000000000L);
    public static final DatabasePartition DB_PARTITION_MESSAGES_BEFORE_PARTIONING = new DatabasePartition("P123", 230701090000000000L);
    public static final DatabasePartition DB_PARTITION_UNTIL_NOW_MINUS_1H = new DatabasePartition("SYS_P111", 230702080000000000L);
    public static final DatabasePartition DB_PARTITION_UNTIL_NOW = new DatabasePartition("SYS_P222", 230702090000000000L);
    public static final DatabasePartition DB_PARTITION_UNTIL_NOW_PLUS_1H = new DatabasePartition("SYS_P333", 230702100000000000L);
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
            result = NOW_AS_NUMBER - TWO_HOURS;

        }};

        List<String> expiredPartitions = messageRetentionPartitionsService.getExpiredPartitionNames(120);

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

        List<String> expiredPartitions = messageRetentionPartitionsService.getExpiredPartitionNames(1);

        assertThat(expiredPartitions, containsInAnyOrder(DB_PARTITION_UNTIL_NOW_MINUS_1H.getPartitionName()));
    }

    @Test
    public void testGetOldestNonDefaultPartition(){
        DatabasePartition oldestNonDefaultPartition = MessageRetentionPartitionsService.getOldestNonDefaultPartition(Arrays.asList(
                DB_PARTITION_DEFAULT,
                DB_PARTITION_MESSAGES_BEFORE_PARTIONING,
                DB_PARTITION_UNTIL_NOW_MINUS_1H,
                DB_PARTITION_UNTIL_NOW_PLUS_1H
        ));
        assertEquals(DB_PARTITION_MESSAGES_BEFORE_PARTIONING, oldestNonDefaultPartition);
    }
}