package eu.domibus.core.earchive.job;

import eu.domibus.api.earchive.EArchiveBatchFilter;
import eu.domibus.api.earchive.EArchiveBatchRequestDTO;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.exceptions.DomibusDateTimeException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.earchive.EArchivingDefaultService;
import eu.domibus.core.util.DateUtilImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_STUCK_IGNORE_RECENT_MINUTES;

/**
 * @since 5.0.7
 * @author Sebastian-Ion TINCU
 */
@Service
public class EArchivingStuckBatchesService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchivingStuckBatchesService.class);

    private final EArchivingDefaultService eArchivingDefaultService;

    private final DomibusPropertyProvider domibusPropertyProvider;

    private final DateUtilImpl dateUtil;

    public EArchivingStuckBatchesService(EArchivingDefaultService eArchivingDefaultService,
                                         DomibusPropertyProvider domibusPropertyProvider, DateUtilImpl dateUtil) {
        this.eArchivingDefaultService = eArchivingDefaultService;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.dateUtil = dateUtil;
    }

    public void reExportStuckBatches() {
        final Integer ignoreMinutes = domibusPropertyProvider.getIntegerProperty(DOMIBUS_EARCHIVE_STUCK_IGNORE_RECENT_MINUTES);
        Date minutesAgo;
        try {
            minutesAgo = dateUtil.getDateMinutesAgo(ignoreMinutes);
        } catch (DomibusDateTimeException e) {
            LOG.error("Please use only positive values greater than 0 for the [{}] property", DOMIBUS_EARCHIVE_STUCK_IGNORE_RECENT_MINUTES, e);
            return;
        }

        EArchiveBatchFilter filter = new EArchiveBatchFilter();
        filter.getStatusList().addAll(EnumSet.of(EArchiveBatchStatus.STARTED));
        filter.setEndDate(minutesAgo);

        List<EArchiveBatchRequestDTO> stuckBatches = eArchivingDefaultService.getBatchRequestList(filter);

        stuckBatches.stream()
                .map(EArchiveBatchRequestDTO::getBatchId)
                .forEach(eArchivingDefaultService::reExportBatch);
    }
}