package eu.domibus.core.util;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.exceptions.DomibusDateTimeException;
import eu.domibus.api.util.DateUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.*;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.ENGLISH;

/**
 * @author Cosmin Baciu
 * @author Sebastian-Ion TINCU
 * @since 3.3
 */
@Component
public class DateUtilImpl implements DateUtil {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DateUtilImpl.class);

    @Override
    public String getIdPkDateHourPrefix(Date value) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT_DEFAULT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(value).substring(0, 8);
    }

    @Override
    public ZonedDateTime getDateHour(String idPk) {
        DateTimeFormatter formatter = ofPattern(DATETIME_FORMAT_DEFAULT).withZone(ZoneOffset.UTC);
        String dateHour = StringUtils.substring(idPk, 0, DATETIME_FORMAT_DEFAULT.length());
        return ZonedDateTime.parse(dateHour, formatter);
    }

    @Override
    public Date fromString(String value) {
        Date result = null;

        if (StringUtils.isNotEmpty(value)) {
            if (StringUtils.isNumeric(value)) {
                result = fromNumber(Long.parseLong(value));
            } else {
                result = fromISO8601(value);
            }
        }

        return result;
    }

    public Timestamp fromNumber(Number value) {
        return new Timestamp(value.longValue());
    }

    public Timestamp fromISO8601(String value) {
        Date date = null;
        try {
            LOG.debug("Parsing an offset date time value: [{}]", value);
            OffsetDateTime dateTime = OffsetDateTime.parse(value);
            date = Date.from(dateTime.toInstant());
        } catch (DateTimeParseException ex) {
            LOG.debug("Error during Parsing offset date time value: [{}]", value);

            try {
                LOG.debug("Parsing local date time value: [{}]", value);
                LocalDateTime dateTime = LocalDateTime.parse(value);
                date = Date.from(dateTime.toInstant(ZoneOffset.UTC));
            } catch (DateTimeParseException exception) {
                LOG.debug("Exception occurred during parsing of date time", exception);
                throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Cannot parse datetime value", exception);
            }
        }

        return new Timestamp(date.getTime());
    }

    @Override
    public Date getStartOfDay() {
        return Date.from(ZonedDateTime.now(ZoneOffset.UTC).withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant());
    }

    @Override
    public String getCurrentTime(DateTimeFormatter dateTimeFormatter) {
        return java.time.LocalDateTime.now(ZoneOffset.UTC).format(dateTimeFormatter);
    }

    @Override
    public Date getDateMinutesAgo(int minutesIntoThePast) throws DomibusDateTimeException {
        if (minutesIntoThePast <= 0) {
            throw new DomibusDateTimeException("Please provide a positive values that's greater than 0 for specifying the number of minutes into the past: minutesIntoThePast=" + minutesIntoThePast);
        }

        return Date.from(ZonedDateTime
                .now(ZoneOffset.UTC)
                .minusMinutes(minutesIntoThePast)
                .toInstant());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getUtcDate() {
        return new Date();
    }

    @Override
    public LocalDateTime getUtcLocalDateTime(LocalDateTime localDateTime){
      return localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    @Override
    public long getDiffMinutesBetweenDates(Date date1, Date date2) {
        long diffInMillies = Math.abs(date2.getTime() - date1.getTime());
        return TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    @Override
    public Long getIdPkDateHour(String date) {
        if (StringUtils.isBlank(date)) {
            throw new DomibusDateTimeException(date, REST_FORMATTER_PATTERNS_MESSAGE);
        }
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(date, REST_FORMATTER);
            String format = getUtcLocalDateTime(localDateTime).format(ofPattern(DATETIME_FORMAT_DEFAULT));
            return Long.parseLong(format + MIN);
        } catch (Exception e) {
            throw new DomibusDateTimeException(date, REST_FORMATTER_PATTERNS_MESSAGE, e);
        }
    }

    @Override
    public long getMaxEntityId(ZonedDateTime instant, long delayInSeconds) {
        long entityId = Long.parseLong(instant
                .minusSeconds(delayInSeconds)
                .format(ofPattern(DATETIME_FORMAT_DEFAULT, ENGLISH)) + MAX);

        LOG.trace("Turned date [{}] delayed by [{}] seconds into MAX entity ID [{}]", instant, delayInSeconds, entityId);
        return entityId;
    }

    @Override
    public long getMinEntityId(ZonedDateTime instant, long delayInSeconds) {
        long entityId = Long.parseLong(instant
                .minusSeconds(delayInSeconds)
                .format(ofPattern(DATETIME_FORMAT_DEFAULT, ENGLISH)) + MIN);

        LOG.trace("Turned date [{}] delayed by [{}] seconds into MIN entity ID [{}]", instant, delayInSeconds, entityId);
        return entityId;
    }
    @Override
    public long getMaxEntityId(long delayInSeconds) {
        return getMaxEntityId(ZonedDateTime.now(ZoneOffset.UTC), delayInSeconds);
    }

    @Override
    public long getMinEntityId(long delayInSeconds) {
        return getMinEntityId(ZonedDateTime.now(ZoneOffset.UTC), delayInSeconds);
    }

    @Override
    public Date convertOffsetDateTimeToDate(OffsetDateTime offsetDateTime) {
        if(offsetDateTime == null) {
            return null;
        }
        return new Date(offsetDateTime.toInstant().toEpochMilli());
    }

    @Override
    public OffsetDateTime convertDateToOffsetDateTime(Date date) {
        if(date == null) {
            return null;
        }
        return date.toInstant().atOffset(ZoneOffset.UTC);
    }
}
