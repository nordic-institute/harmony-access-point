package eu.domibus.plugin.convert;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.core.convert.converter.Converter;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * {@code String} to {@code TemporalAccessor} converter accepting date-time, date or time inputs with or without
 * timezone offsets.
 *
 * @author Sebastian-Ion TINCU
 * @since 5.0
 */
public class StringToTemporalAccessorConverter implements Converter<String, TemporalAccessor> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(StringToTemporalAccessorConverter.class);

    private final DateTimeFormatter formatter;

    public StringToTemporalAccessorConverter(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    /**
     * Converts a source input date-time, date or time, with or without a timezone to a UTC temporal.
     *
     * @param source the input date-time, date or time, with or without a timezone and/or a timezone offset.
     * @return a temporal in the UTC timezone.
     */
    @Override
    public TemporalAccessor convert(String source) {
        if (source == null) {
            LOG.info("Returning null temporal for null input string");
            return null;
        }

        TemporalAccessor result = formatter.parseBest(source,
                OffsetDateTime::from, OffsetTime::from, LocalDateTime::from, LocalDate::from, LocalTime::from);
        if (result instanceof OffsetDateTime) {
            LOG.debug("Unmarshalling an offset date time");
            result = ((OffsetDateTime) result).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
        } else  if (result instanceof OffsetTime) {
            LOG.debug("Unmarshalling a local date time with timezone offset");
            result = ((OffsetTime) result).withOffsetSameInstant(ZoneOffset.UTC).toLocalTime();
        } else if (result instanceof LocalDateTime) {
            LOG.debug("Unmarshalling a local date time without timezone offset");
        } else if (result instanceof LocalDate) {
            LOG.debug("Unmarshalling a local date with or without timezone offset");
        } else if (result instanceof LocalTime) {
            LOG.debug("Unmarshalling a local time without zone offset");
        }

        LOG.info("Returning temporal [{}]", result);
        return result;
    }
}
