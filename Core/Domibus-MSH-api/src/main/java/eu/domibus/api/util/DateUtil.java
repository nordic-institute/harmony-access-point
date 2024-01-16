package eu.domibus.api.util;

import eu.domibus.api.exceptions.DomibusDateTimeException;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.Locale;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface DateUtil {

    DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    String DATETIME_FORMAT_DEFAULT = "yyMMddHH";

    String REST_FORMATTER_PATTERNS_MESSAGE = "[yyyy-MM-dd'T'HH'H'] or [yyyy-MM-dd]";

    DateTimeFormatter REST_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_DATE)

            .optionalStart()
            .appendLiteral('T')
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral('H')
            .optionalEnd()

            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
            .toFormatter(Locale.ENGLISH);

    ZonedDateTime getDateHour(String idPk);

    Date fromString(String value);

    Date getStartOfDay();

    String getCurrentTime(DateTimeFormatter dateTimeFormatter);

    /**
     * Returns a past {@code Date} that reflects an instant of time that is minutes ago from the current system {@code Date}.
     * The exact number of minutes into the past is passed in as a parameter and must be a positive non-zero value;  a
     * {@code DomibusDateTimeException} exception is thrown otherwise. The resulting {@Date} is reflected in
     * coordinated universal time (UTC).
     *
     * @param minutesIntoThePast how many minutes ago should the resulting date be
     * @return a UTC date that is taken from an instant that happened exactly the number of minutes ago
     * @throws eu.domibus.api.exceptions.DomibusDateTimeException if the minutes has a negative value or is equal to {@code 0}.
     */
    Date getDateMinutesAgo(int minutesIntoThePast) throws DomibusDateTimeException;

    /**
     * Returns the current system {@code Date}, reflected in coordinated universal time (UTC).
     *
     * @return the current system {@code Date}, reflected in coordinated universal time (UTC)
     */
    Date getUtcDate();

    /**
     * @param localDateTime
     * @return the localDateTime reflected in coordinated universal time (UTC)
     */
    LocalDateTime getUtcLocalDateTime(LocalDateTime localDateTime);

    long getDiffMinutesBetweenDates(Date date1, Date date2);

    /**
     * Parse a string date to an ID_PK
     *
     * @param date of format YYYY-MM-dd'T'HH'H' or YYYY-MM-dd with formatter {@link #REST_FORMATTER}
     * @return date of format YYMMDDHH0000000000
     */
    Long getIdPkDateHour(String date);

    /**
     * Parse a date to an ID_PK prefix
     *
     * @return string of format YYMMDDHH
     */
    String getIdPkDateHourPrefix(Date value);

    long getMaxEntityId(ZonedDateTime instant, long delayInSeconds);

    long getMinEntityId(ZonedDateTime instant, long delayInSeconds);

    long getMaxEntityId(long delayInSeconds);

    long getMinEntityId(long delayInSeconds);

    Date convertOffsetDateTimeToDate(OffsetDateTime offsetDateTime);

    OffsetDateTime convertDateToOffsetDateTime(Date date);
}
