package eu.domibus.core.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    public DateTimeFormatter dateTimeFormatter;

    public Date fromString(String dateString) {
        final LocalDateTime localDateTime = LocalDateTime.parse(dateString, dateTimeFormatter);
        return Date.from(localDateTime.atZone(ZoneOffset.UTC).toInstant());
    }
}
