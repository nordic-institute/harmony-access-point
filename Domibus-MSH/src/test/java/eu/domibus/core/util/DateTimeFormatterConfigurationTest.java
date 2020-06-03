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

    private void parse(String s) {
        LocalDateTime.parse(s, dateTimeFormatter);
    }

    @Test
    public void format() {
        parse("2020-06-02T20:00:00");
    }

    @Test
    public void format_UTC() {
        parse("2020-06-02T20:00:00Z");
    }

    @Test
    public void format_FractionalSeconds_UTC() {
        parse("2020-06-02T09:00:00.000Z");
    }

    @Test
    public void format_FractionalSeconds_timeZone() {
        parse("2020-06-02T23:00:00.000+03:00");
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