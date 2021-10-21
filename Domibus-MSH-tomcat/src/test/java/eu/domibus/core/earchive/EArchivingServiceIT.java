package eu.domibus.core.earchive;

import eu.domibus.AbstractIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.MIN;
import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.dtf;
import static eu.domibus.core.earchive.EArchivingDefaultService.CONTINUOUS_ID;
import static eu.domibus.core.earchive.EArchivingDefaultService.SANITY_ID;

/**
 * @author François Gautier
 * @since 5.0
 */
public class EArchivingServiceIT extends AbstractIT {

    @Autowired
    private EArchivingDefaultService eArchivingService;

    @Autowired
    private EArchiveBatchStartDao eArchiveBatchStartDao;

    @Before
    public void setUp() throws Exception {
        Assert.assertEquals(101000000000000L, ((long) eArchiveBatchStartDao.findByReference(CONTINUOUS_ID).getLastPkUserMessage()));
        Assert.assertEquals(101000000000000L, ((long) eArchiveBatchStartDao.findByReference(SANITY_ID).getLastPkUserMessage()));
    }

    @Test
    @Transactional
    public void updateStartDateContinuousArchive() {

        Date startDate = new Date();
        eArchivingService.updateStartDateContinuousArchive(startDate);

        Assert.assertEquals(Long.parseLong(ZonedDateTime.ofInstant(startDate.toInstant(), ZoneOffset.UTC).format(dtf) + MIN),
                ((long) eArchiveBatchStartDao.findByReference(CONTINUOUS_ID).getLastPkUserMessage()));

    }

    @Test
    @Transactional
    public void getStartDateContinuousArchive() {

        Date startDateContinuousArchive = eArchivingService.getStartDateContinuousArchive();

        Assert.assertEquals(Date.from(LocalDate.of(2000, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant()), startDateContinuousArchive);
    }

    @Test
    @Transactional
    public void updateStartDateSanityArchive() {
        Date startDate = new Date();
        eArchivingService.updateStartDateSanityArchive(startDate);
        Assert.assertEquals(Long.parseLong(ZonedDateTime.ofInstant(startDate.toInstant(), ZoneOffset.UTC).format(dtf) + MIN),
                ((long) eArchiveBatchStartDao.findByReference(SANITY_ID).getLastPkUserMessage()));

    }

    @Test
    @Transactional
    public void getStartDateSanityArchive() {

        Date startDateSanityArchive = eArchivingService.getStartDateSanityArchive();

        Assert.assertEquals(Date.from(LocalDate.of(2000, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant()), startDateSanityArchive);
    }
}
