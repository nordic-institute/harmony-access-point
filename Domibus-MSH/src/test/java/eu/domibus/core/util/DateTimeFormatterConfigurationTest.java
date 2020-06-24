package eu.domibus.core.util;

import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATE_TIME_PATTERN_ON_RECEIVING;

/**
 * @author François Gautier
 * @since 4.2
 */
@RunWith(JMockit.class)
public class DateTimeFormatterConfigurationTest {

    public static final String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS][.SSSSSS][.SSS][z]";
    @Tested
    private DateTimeFormatterConfiguration dateTimeFormatterConfiguration;

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    @Before
    public void setUp() {
        new Expectations(){{
            domibusPropertyProvider.getProperty(DOMIBUS_DATE_TIME_PATTERN_ON_RECEIVING);
            result = DEFAULT_DATE_TIME_PATTERN;
            times = 1;
        }};
    }

    @After
    public void tearDown() {
        new FullVerifications(){};
    }

    private void parse(String s) {
        LocalDateTime.parse(s, dateTimeFormatterConfiguration.dateTimeFormatter());
    }

    @Test
    public void format() {
        parse("2020-06-02T20:00:00");
    }

    @Test
    public void format_frac() {
        parse("2020-06-02T20:00:00.000");
    }

    @Test
    public void format_frac6() {
        parse("2020-06-02T20:00:00.000000");
    }

    @Test
    public void format_frac9() {
        parse("2020-06-02T20:00:00.000000000");
    }

    @Test(expected = DateTimeParseException.class)
    public void format_frac7() {
        parse("2020-06-02T20:00:00.0000000");
    }

    @Test
    public void format_UTC() {
        parse("2020-06-02T20:00:00Z");
    }

    @Test(expected = DateTimeParseException.class)
    public void format_FractionalSeconds1_UTC() {
        parse("2020-06-02T09:00:00.0Z");
    }

    /**
     * Strangely enough, this is an accepted
     */
    @Test
    public void format_FractionalSeconds2_UTC() {
        parse("2020-06-02T09:00:00.12Z");
    }

    @Test(expected = DateTimeParseException.class)
    public void format_FractionalSeconds2() {
        parse("2020-06-02T09:00:00.12");
    }

    @Test
    public void format_FractionalSeconds3_UTC() {
        parse("2020-06-02T09:00:00.000Z");
    }

    @Test
    public void format_FractionalSeconds_timeZone() {
        parse("2020-06-02T23:00:00.000+03:00");
    }

    @Test
    public void format_FractionalSeconds6_timeZone() {
        parse("2020-06-02T23:00:00.000000+03:00");
    }

    @Test
    public void format_FractionalSeconds9_timeZone() {
        parse("2020-06-02T23:00:00.000000000+03:00");
    }

    @Test(expected = DateTimeParseException.class)
    public void format_FractionalSeconds12_timeZone() {
        parse("2020-06-02T23:00:00.000000000000+03:00");
    }

    @Test
    public void format_timeZone() {
        parse("2020-06-02T23:00:00+03:00");
    }

    @Test(expected = DateTimeParseException.class)
    public void format_z() {
        parse("2000-03-04T20:00:00z");
    }
}