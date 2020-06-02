package eu.domibus.core.util;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * @author François Gautier
 * @since 4.2
 */
public class DateTimeFormatterConfigurationTest {

    private DateTimeFormatter dateTimeFormatter;

    @Before
    public void setUp() {
        dateTimeFormatter = new DateTimeFormatterConfiguration().dateTimeFormatter();
    }

    @Test
    public void format() {
        LocalDateTime.parse("2020-06-02T20:00:00", dateTimeFormatter);
    }

    @Test
    public void format_UTC() {
        LocalDateTime.parse("2020-06-02T20:00:00Z", dateTimeFormatter);
    }

    @Test
    public void format_FractionalSeconds_UTC() {
        LocalDateTime.parse("2020-06-02T09:00:00.000Z", dateTimeFormatter);
    }

    @Test
    public void format_FractionalSeconds_timeZone() {
        LocalDateTime.parse("2020-06-02T23:00:00.000+03:00", dateTimeFormatter);
    }

    @Test
    public void format_timeZone() {
        LocalDateTime.parse("2020-06-02T23:00:00+03:00", dateTimeFormatter);
    }

    @Test(expected = DateTimeParseException.class)
    public void format_z() {
        LocalDateTime.parse("2000-03-04T20:00:00z", dateTimeFormatter);
    }
}