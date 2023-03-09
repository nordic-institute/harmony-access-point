package eu.domibus.core.message.retention;

import eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DateUtil;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.EventProperties;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PARTITIONS_CREATION_DAYS_TO_CHECK;

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
