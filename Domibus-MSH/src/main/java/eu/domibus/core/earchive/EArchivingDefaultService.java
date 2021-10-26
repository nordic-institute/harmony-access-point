package eu.domibus.core.earchive;

import eu.domibus.api.earchive.DomibusEArchiveService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
public class EArchivingDefaultService implements DomibusEArchiveService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchivingDefaultService.class);

    public static final int CONTINUOUS_ID = 1;

    public static final int SANITY_ID = 2;

    private final EArchiveBatchStartDao eArchiveBatchStartDao;

    public EArchivingDefaultService(EArchiveBatchStartDao eArchiveBatchStartDao) {
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

    @Override
    public Date getStartDateContinuousArchive() {
        LocalDate parse = LocalDate.parse(StringUtils.substring(StringUtils.leftPad(eArchiveBatchStartDao.findByReference(CONTINUOUS_ID).getLastPkUserMessage() + "", 18, "0"), 0, 8), dtf);
        return Date.from(parse.atStartOfDay(ZoneOffset.UTC).toInstant());
    }

    @Override
    public Date getStartDateSanityArchive() {
        LocalDate parse = LocalDate.parse(StringUtils.substring(StringUtils.leftPad(eArchiveBatchStartDao.findByReference(SANITY_ID).getLastPkUserMessage() + "", 18, "0") , 0, 8), dtf);
        return Date.from(parse.atStartOfDay(ZoneOffset.UTC).toInstant());
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
