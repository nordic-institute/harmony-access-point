package eu.domibus.api.util;

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

    Date fromString(String value);

    Date getStartOfDay();

    String getCurrentTime(DateTimeFormatter dateTimeFormatter);

    String getCurrentTime();

    /**
     * Returns the current system {@code Date}, reflected in coordinated universal time (UTC).
     *
     * @return the current system {@code Date}, reflected in coordinated universal time (UTC)
     */
    Date getUtcDate();

    long getDiffMinutesBetweenDates(Date date1, Date date2);

    /**
     * Parse a string date to an ID_PK
     *
     * @param date of format YYYY-MM-dd'T'HH'H' or YYYY-MM-dd with formatter {@link #REST_FORMATTER}
     * @return date of format YYMMDDHH0000000000
     */
    Long getIdPkDateHour(String date);

    String getIdPkDateHourPrefix(Date value);
}
