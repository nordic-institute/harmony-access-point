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

    protected DateUtil dateUtil;


    public PartitionService(DateUtil dateUtil) {
        this.dateUtil = dateUtil;
    }

    public Long getPartitionHighValueFromDate(Date partitionDate) {
        Long highValue = new Long (dateUtil.getIdPkDateHourPrefix(partitionDate) + DomibusDatePrefixedSequenceIdGeneratorGenerator.MIN);
        LOG.debug("Get partition highValue from date [{}], highValue [{}]", partitionDate, highValue);
        return highValue;
    }
}
