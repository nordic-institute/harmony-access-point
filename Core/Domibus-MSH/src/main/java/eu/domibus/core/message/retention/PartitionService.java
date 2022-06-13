package eu.domibus.core.message.retention;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DateUtil;
import eu.domibus.core.alerts.configuration.partitions.PartitionsConfigurationManager;
import eu.domibus.core.alerts.configuration.partitions.PartitionsModuleConfiguration;
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

    protected UserMessageDao userMessageDao;

    protected EventService eventService;

    protected DateUtil dateUtil;

    protected DomibusPropertyProvider domibusPropertyProvider;

    protected PartitionsConfigurationManager partitionsConfigurationManager;

    public PartitionService(UserMessageDao userMessageDao,
                            EventService eventService,
                            DateUtil dateUtil,
                            DomibusPropertyProvider domibusPropertyProvider,
                            PartitionsConfigurationManager partitionsConfigurationManager) {
        this.userMessageDao = userMessageDao;
        this.eventService = eventService;
        this.dateUtil = dateUtil;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.partitionsConfigurationManager = partitionsConfigurationManager;
    }

    public void verifyPartitionsInAdvance() {
        Integer daysToCheckPartitions = domibusPropertyProvider.getIntegerProperty(DOMIBUS_PARTITIONS_CREATION_DAYS_TO_CHECK);
        LOG.debug("Verify if partitions were created properly");
        Date latestPartitionToCheckDate = DateUtils.addDays(dateUtil.getUtcDate(), daysToCheckPartitions);
        String partitionName = getPartitionNameFromDate(latestPartitionToCheckDate);
        Boolean partitionExists = userMessageDao.checkPartitionExists(partitionName);
        if (BooleanUtils.isFalse(partitionExists)) {
            LOG.warn("Throw partition creation warning, this partition was expected to exist but could not be found [{}]", partitionName);
            PartitionsModuleConfiguration partitionsModuleConfiguration = partitionsConfigurationManager.getConfiguration();
            if (partitionsModuleConfiguration.isActive()) {
                eventService.enqueuePartitionCheckEvent(partitionName);
            }
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
