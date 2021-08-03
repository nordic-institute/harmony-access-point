package eu.domibus.plugin.convert;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * {@code String} to {@code LocalDateTime} converter accepting date-time, date or time inputs with or without timezone.
 *
 * @author Sebastian-Ion TINCU
 * @since 5.0
 */
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(StringToLocalDateTimeConverter.class);

    private final DateTimeFormatter formatter;

    public StringToLocalDateTimeConverter(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    /**
     * Converts a source input date-time, date or time, with or without a timezone to an instant in UTC.
     *
     * @param source the input date-time, date or time, with or without a timezone and/or a timezone offset.
     * @return a LocalDateTime instant in the UTC time zone offset.
     */
    @Override
    public LocalDateTime convert(String source) {
        if (source == null) {
            LOG.debug("Returning null local date time for null input string");
            return null;
        }

        LocalDateTime result = null;

        TemporalAccessor dt = formatter.parseBest(source, ZonedDateTime::from, LocalDateTime::from);
        if (dt instanceof ZonedDateTime) {
            LOG.debug("Unmarshalling a local date time with time zone extra information");
            result = ((ZonedDateTime) dt).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        } else if (dt instanceof LocalDateTime) {
            LOG.debug("Unmarshalling a local date time without time zone extra information");
            result = (LocalDateTime) dt;
        }

        LOG.debug("Returning local date time [{}]", result);
        return result;
    }
}
