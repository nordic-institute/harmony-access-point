package eu.domibus.core.earchive;

import eu.domibus.api.earchive.DomibusEArchiveService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.MIN;
import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.dtf;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class EArchivingService implements DomibusEArchiveService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchivingService.class);

    public static final int CONTINUOUS_ID = 1;

    public static final int SANITY_ID = 2;

    private final EArchiveBatchStartDao eArchiveBatchStartDao;

    public EArchivingService(EArchiveBatchStartDao eArchiveBatchStartDao) {
        this.eArchiveBatchStartDao = eArchiveBatchStartDao;
    }

    @Override
    public void updateStartDateContinuousArchive(Date startDate) {
        updateEArchiveBatchStart(CONTINUOUS_ID, startDate);
    }

    @Override
    public void updateStartDateSanityArchive(Date startDate) {
        updateEArchiveBatchStart(SANITY_ID, startDate);
    }

    private void updateEArchiveBatchStart(int sanityId, Date startDate) {
        EArchiveBatchStart byReference = eArchiveBatchStartDao.findByReference(sanityId);
        long lastPkUserMessage = Long.parseLong(ZonedDateTime.ofInstant(startDate.toInstant(), ZoneOffset.UTC).format(dtf) + MIN);
        if (LOG.isDebugEnabled()) {
            LOG.debug("New start date archive [{}] batch lastPkUserMessage : [{}]", byReference.getDescription(), lastPkUserMessage);
        }
        byReference.setLastPkUserMessage(lastPkUserMessage);
        eArchiveBatchStartDao.update(byReference);
    }
}
