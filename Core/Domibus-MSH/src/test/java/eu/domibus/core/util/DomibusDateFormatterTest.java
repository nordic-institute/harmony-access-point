package eu.domibus.core.util;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author François Gautier
 * @since 4.2
 */
public class DomibusDateFormatterTest {

    public static final String DEFAULT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS][.SSSSSS][.SSS][z]";
    public static final String VALID_DATE_STRING = "2020-06-02T20:00:00";
    private DomibusDateFormatter domibusDateFormatter;

    @Before
    public void setUp() {
        domibusDateFormatter = new DomibusDateFormatter(DateTimeFormatter.ofPattern(DEFAULT_PATTERN));
    }

    @Test
    public void fromString_ok() {
        Date date = domibusDateFormatter.fromString(VALID_DATE_STRING);
        LocalDateTime from = Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();

        Assert.assertEquals(2020, from.getYear());
        Assert.assertEquals(20, from.getHour());
        Assert.assertEquals(0, from.getMinute());
        Assert.assertEquals(0, from.getSecond());
    }

    @Test
    public void fromString_exception() {
        try {
            domibusDateFormatter.fromString("tomorrow");
            Assert.fail();
        } catch (DomibusCoreException e) {
            Assert.assertThat(e.getError(), Is.is(DomibusCoreErrorCode.DOM_007));
        }

    }
}