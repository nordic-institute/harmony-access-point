package eu.domibus.core.util;

import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATE_TIME_PATTERN_ON_RECEIVING;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATE_TIME_PATTERN_ON_RECEIVING_USE_STANDARD_FORMAT;

/**
 * @author François Gautier
 * @since 4.2
 */
@RunWith(Enclosed.class)
public class DateTimeFormatterConfigurationTest {

    public static class WhenUsingCustomPattern {

        public static final String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS][.SSSSSS][.SSS][z]";

        @Injectable
        private DomibusPropertyProvider domibusPropertyProvider;

        @Tested
        private DateTimeFormatterConfiguration dateTimeFormatterConfiguration;

        private DateTimeFormatter getFormatter() {
            new Expectations() {{
                domibusPropertyProvider.getBooleanProperty(DOMIBUS_DATE_TIME_PATTERN_ON_RECEIVING_USE_STANDARD_FORMAT);
                result = false;

                domibusPropertyProvider.getProperty(DOMIBUS_DATE_TIME_PATTERN_ON_RECEIVING);
                result = DEFAULT_DATE_TIME_PATTERN;
            }};

            return dateTimeFormatterConfiguration.dateTimeFormatter();
        }

        private void parse(String s) {
            getFormatter().parse(s);
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

    public static class WhenUsingStandardFormat {

        @Injectable
        private DomibusPropertyProvider domibusPropertyProvider;

        @Tested
        private DateTimeFormatterConfiguration dateTimeFormatterConfiguration;

        private DateTimeFormatter getFormatter() {
            new Expectations() {{
                domibusPropertyProvider.getBooleanProperty(DOMIBUS_DATE_TIME_PATTERN_ON_RECEIVING_USE_STANDARD_FORMAT);
                result = true;
            }};

            DateTimeFormatter formatter = dateTimeFormatterConfiguration.dateTimeFormatter();

            new Verifications() {{
                domibusPropertyProvider.getProperty(DOMIBUS_DATE_TIME_PATTERN_ON_RECEIVING);
                times = 0;
            }};
            return formatter;
        }

        private void parse(String s) {
            getFormatter().parse(s);
        }

        @Test
        public void should_ParseWithoutTimezone() {
            parse("2020-06-02T20:00:00");
        }

        @Test
        public void should_ParseUTC_Z() {
            parse("2020-06-02T20:00:00Z");
        }

        @Test
        public void should_ParseLeapDay() {
            parse("2024-02-29T11:22:33Z");
        }

        @Test
        public void should_ParseFraction_1Digit() {
            parse("2020-06-02T09:00:00.0");
        }

        @Test
        public void should_ParseFraction_2Digits_UTC() {
            parse("2020-06-02T09:00:00.12Z");
        }

        @Test
        public void should_ParseFraction_2Digits_NoTimezone() {
            parse("2020-06-02T09:00:00.12");
        }

        @Test
        public void should_ParseFraction_3Digits_NoTimezone() {
            parse("2020-06-02T20:00:00.000");
        }

        @Test
        public void should_ParseFraction_3Digits_UTC() {
            parse("2020-06-02T09:00:00.000Z");
        }

        @Test
        public void should_ParseFraction_3Digits_WithOffset() {
            parse("2020-06-02T23:00:00.000+03:00");
        }

        @Test
        public void should_ParseFraction_6Digits_NoTimezone() {
            parse("2020-06-02T20:00:00.000000");
        }

        @Test
        public void should_ParseFraction_6Digits_WithOffset() {
            parse("2020-06-02T23:00:00.000000+03:00");
        }

        @Test
        public void should_ParseFraction_7Digits() {
            parse("2020-06-02T20:00:00.0000000");
        }

        @Test
        public void should_ParseFraction_9Digits_NoTimezone() {
            parse("2020-06-02T20:00:00.000000000");
        }

        @Test
        public void should_ParseFraction_9Digits_WithOffset() {
            parse("2020-06-02T23:00:00.000000000+03:00");
        }

        @Test
        public void should_ParseWithPositiveOffset() {
            parse("2020-06-02T23:00:00+03:00");
        }

        @Test
        public void should_ParseWithZeroOffset() {
            parse("2021-10-05T12:15:30+00:00");
        }

        @Test
        public void should_ParseWithNegativeOffset() {
            parse("2025-01-20T10:00:00-13:00");
        }

        @Test(expected = DateTimeParseException.class)
        public void shouldFail_InvalidDay() {
            parse("2021-04-31T10:15:30Z");
        }

        @Test(expected = DateTimeParseException.class)
        public void shouldFail_InvalidDateTimeSeparator() {
            parse("2021-10-05 10:15:30Z");
        }

        @Test(expected = DateTimeParseException.class)
        public void shouldFail_InvalidLeapSecond() {
            parse("2021-12-31T23:59:60Z");
        }

        @Test(expected = DateTimeParseException.class)
        public void shouldFail_InvalidCommaFraction() {
            parse("2021-10-05T10:15:30,123Z");
        }

        @Test(expected = DateTimeParseException.class)
        public void shouldFail_FractionTooLong() {
            parse("2020-06-02T23:00:00.000000000000+03:00");
        }

        @Test(expected = DateTimeParseException.class)
        public void shouldFail_LowercaseZ() {
            parse("2000-03-04T20:00:00z");
        }

        @Test(expected = DateTimeParseException.class)
        public void shouldFail_OffsetWithoutColon() {
            parse("2021-10-05T12:15:30+0200");
        }
    }
}