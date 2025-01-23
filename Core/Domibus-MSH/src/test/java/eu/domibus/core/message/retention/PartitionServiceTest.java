package eu.domibus.core.message.retention;

import eu.domibus.api.model.DatabasePartition;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static eu.domibus.core.message.retention.PartitionService.DEFAULT_PARTITION;
import static org.junit.Assert.assertEquals;

/**
 * @author François Gautier
 * @since 5.1
 */
public class PartitionServiceTest {
    public static final DatabasePartition DB_PARTITION_DEFAULT = new DatabasePartition(DEFAULT_PARTITION, 220000000000000000L);
    public static final DatabasePartition DB_PARTITION_MESSAGES_BEFORE_PARTIONING = new DatabasePartition("P123", 230701090000000000L);
    public static final DatabasePartition DB_PARTITION_UNTIL_NOW_MINUS_1H = new DatabasePartition("SYS_P111", 230702080000000000L);
    public static final DatabasePartition DB_PARTITION_UNTIL_NOW = new DatabasePartition("SYS_P222", 230702090000000000L);
    public static final DatabasePartition DB_PARTITION_UNTIL_NOW_PLUS_1H = new DatabasePartition("SYS_P333", 230702100000000000L);
    private PartitionService partitionService;


    @Before
    public void setUp() throws Exception {
        partitionService = new PartitionService(null);
    }

    @Test
    public void testGetNewestNonDefaultPartition(){
        DatabasePartition newestNonDefaultPartition = partitionService.getNewestNonDefaultPartition(Arrays.asList(
                DB_PARTITION_DEFAULT,
                DB_PARTITION_MESSAGES_BEFORE_PARTIONING,
                DB_PARTITION_UNTIL_NOW_MINUS_1H,
                DB_PARTITION_UNTIL_NOW_PLUS_1H
        ));
        assertEquals(DB_PARTITION_UNTIL_NOW_PLUS_1H, newestNonDefaultPartition);
    }
}
