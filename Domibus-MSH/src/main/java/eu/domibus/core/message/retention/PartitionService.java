package eu.domibus.core.message.retention;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DateUtil;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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

    protected PModeProvider pModeProvider;

    protected UserMessageDao userMessageDao;

    protected UserMessageLogDao userMessageLogDao;

    protected DomibusPropertyProvider domibusPropertyProvider;

    protected EventService eventService;

    protected DomibusConfigurationService domibusConfigurationService;

    protected DomainService domainService;

    protected DomainContextProvider domainContextProvider;

    protected DateUtil dateUtil;

    public static final String DEFAULT_PARTITION_NAME = "P22000000"; // default partition that we never delete

    public PartitionService(PModeProvider pModeProvider,
                            UserMessageDao userMessageDao,
                            UserMessageLogDao userMessageLogDao,
                            DomibusPropertyProvider domibusPropertyProvider,
                            EventService eventService,
                            DomibusConfigurationService domibusConfigurationService,
                            DomainService domainService,
                            DomainContextProvider domainContextProvider, DateUtil dateUtil) {
        this.pModeProvider = pModeProvider;
        this.userMessageDao = userMessageDao;
        this.userMessageLogDao = userMessageLogDao;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.eventService = eventService;
        this.domibusConfigurationService = domibusConfigurationService;
        this.domainService = domainService;
        this.domainContextProvider = domainContextProvider;
        this.dateUtil = dateUtil;
    }

    public void verifyPartitionsInAdvance() {
        LOG.debug("Verify if partitions were created properly");
        Date latestPartitionToCheckDate = DateUtils.addDays(dateUtil.getUtcDate(), DAYS_TO_CHECK_PARTITIONS);;
        String partitionName = getPartitionNameFromDate(latestPartitionToCheckDate);
        Boolean partitionExists = userMessageDao.checkPartitionExists(partitionName);
        if(!partitionExists) {
            LOG.warn("Throw partition creation warning, this partition was expected to exist but could not be found [{{}]", partitionName);
            eventService.enqueuePartitionCheckEvent(partitionName);
        }
    }

    public String getPartitionNameFromDate(Date partitionDate) {
        String partitionName = 'P' + dateUtil.getIdPkDateHourPrefix(partitionDate);
        LOG.debug("Get partition name from date [{}], partitionName [{}]", partitionDate, partitionName);
        return partitionName;
    }

}
