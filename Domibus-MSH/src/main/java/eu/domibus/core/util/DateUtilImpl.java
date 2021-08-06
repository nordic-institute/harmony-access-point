package eu.domibus.core.util;

import eu.domibus.api.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Cosmin Baciu
 * @author Sebastian-Ion TINCU
 * @since 3.3
 */
@Component
public class DateUtilImpl implements DateUtil {

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS][.SSSSSS][.SSS][z]");
        LocalDateTime dateTime = LocalDateTime.parse(value, formatter);
        Date date = Date.from(dateTime.atZone(ZoneOffset.UTC).toInstant());
        // LocalDateTime dateTime1 = LocalDateTime.of(value)
        return new Timestamp(date.getTime());
    }

    @Override
    public Date getStartOfDay() {
        return Date.from(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0).atZone(ZoneOffset.UTC).toInstant());
    }

    @Override
    public String getCurrentTime(DateTimeFormatter dateTimeFormatter) {
        return java.time.LocalDateTime.now().format(dateTimeFormatter);
    }

    @Override
    public String getCurrentTime() {
        return getCurrentTime(DEFAULT_FORMATTER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getUtcDate() {
        return new Date();
    }

    @Override
    public long getDiffMinutesBetweenDates(Date date1, Date date2) {
        long diffInMillies = Math.abs(date2.getTime() - date1.getTime());
        return TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }
}
