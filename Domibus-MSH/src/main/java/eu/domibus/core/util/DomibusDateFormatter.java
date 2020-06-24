package eu.domibus.core.util;

import eu.domibus.api.exceptions.DomibusDateTimeException;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class DomibusDateFormatter {

    protected final DateTimeFormatter dateTimeFormatter;

    public DomibusDateFormatter(DateTimeFormatter dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }

    /**
     * Obtains an instance of {@link Date} from a text string using a specific formatter
     * (see property {@link eu.domibus.api.property.DomibusPropertyMetadataManagerSPI#DOMIBUS_DATE_TIME_PATTERN_ON_RECEIVING}).
     * The text is parsed using the formatter, returning a date-time ({@link ZoneOffset#UTC})
     *
     * @param dateString the text to parse, not null
     * @return the parsed local date-time at time zone ({@link ZoneOffset#UTC}), not null
     * @throws DomibusDateTimeException in case the dateString is not following the right pattern
     */
    public Date fromString(String dateString) throws DomibusDateTimeException {
        try {
            final LocalDateTime localDateTime = LocalDateTime.parse(dateString, dateTimeFormatter);
            return Date.from(localDateTime.atZone(ZoneOffset.UTC).toInstant());
        } catch (DateTimeException e) {
            throw new DomibusDateTimeException(dateString);
        }
    }
}
