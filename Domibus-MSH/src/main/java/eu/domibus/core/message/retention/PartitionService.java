package eu.domibus.core.message.retention;

import eu.domibus.api.util.DateUtil;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * This service class is responsible for the handling of partitions
 *
 * @author idragusa
 * @since 5.0
 */
@Service
public class PartitionService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartitionService.class);

    public static final Integer DAYS_TO_CHECK_PARTITIONS = 7;

    protected UserMessageDao userMessageDao;

    protected EventService eventService;

    protected DateUtil dateUtil;

    public PartitionService(UserMessageDao userMessageDao,
                            EventService eventService,
                            DateUtil dateUtil) {
        this.userMessageDao = userMessageDao;
        this.eventService = eventService;
        this.dateUtil = dateUtil;
    }

    public void verifyPartitionsInAdvance() {
        LOG.debug("Verify if partitions were created properly");
        Date latestPartitionToCheckDate = DateUtils.addDays(dateUtil.getUtcDate(), DAYS_TO_CHECK_PARTITIONS);
        String partitionName = getPartitionNameFromDate(latestPartitionToCheckDate);
        Boolean partitionExists = userMessageDao.checkPartitionExists(partitionName);
        if (BooleanUtils.isFalse(partitionExists)) {
            LOG.warn("Throw partition creation warning, this partition was expected to exist but could not be found [{}]", partitionName);
            eventService.enqueuePartitionCheckEvent(partitionName);
            return;
        }
        LOG.debug("Partitions check successful, checked partitionName [{}].", partitionName);
    }

    public String getPartitionNameFromDate(Date partitionDate) {
        String partitionName = 'P' + dateUtil.getIdPkDateHourPrefix(partitionDate);
        LOG.debug("Get partition name from date [{}], partitionName [{}]", partitionDate, partitionName);
        return partitionName;
    }

}
