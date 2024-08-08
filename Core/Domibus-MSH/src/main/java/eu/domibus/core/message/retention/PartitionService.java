package eu.domibus.core.message.retention;

import eu.domibus.api.model.DatabasePartition;
import eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator;
import eu.domibus.api.util.DateUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * This service class is responsible for the handling of partitions
 *
 * @author idragusa
 * @since 5.0
 */
@Service
public class PartitionService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartitionService.class);

    public static final String PARTITION_NAME_REGEXP = "SYS_P[0-9]+|P[0-9]+";

    public static final String DEFAULT_PARTITION = "P1970"; // default partition that we never delete

    protected DateUtil dateUtil;


    public PartitionService(DateUtil dateUtil) {
        this.dateUtil = dateUtil;
    }

    public Long getPartitionHighValueFromDate(Date partitionDate) {
        Long highValue = new Long (dateUtil.getIdPkDateHourPrefix(partitionDate) + DomibusDatePrefixedSequenceIdGeneratorGenerator.MIN);
        LOG.debug("Get partition highValue from date [{}], highValue [{}]", partitionDate, highValue);
        return highValue;
    }

    public DatabasePartition getNewestNonDefaultPartition(List<DatabasePartition> partitions) {
        return partitions.stream()
                .filter(p -> !DEFAULT_PARTITION.equalsIgnoreCase(p.getPartitionName()))
                .max(Comparator.comparing(DatabasePartition::getHighValue))
                .orElse(null);
    }
}
