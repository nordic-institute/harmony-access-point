package eu.domibus.core.csv.serializer;

import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static eu.domibus.core.csv.CsvServiceImpl.CSV_DATE_PATTERN;

/**
 * @author François Gautier
 * @since 4.2
 */
@Service
public class CvsSerializerDate implements CvsSerializer {

    @Override
    public boolean canHandle(Object fieldValue) {
        return fieldValue instanceof Date;
    }

    @Override
    public String serialize(Object fieldValue) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern(CSV_DATE_PATTERN);
        ZonedDateTime d = ZonedDateTime.ofInstant(((Date)fieldValue).toInstant(), ZoneId.systemDefault());
        return d.format(f);
    }
}
