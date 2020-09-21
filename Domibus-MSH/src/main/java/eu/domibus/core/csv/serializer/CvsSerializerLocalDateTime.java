package eu.domibus.core.csv.serializer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.function.Predicate;

import static eu.domibus.core.csv.CsvServiceImpl.CSV_DATE_PATTERN;

/**
 * @author François Gautier
 * @since 4.2
 */
public class CvsSerializerLocalDateTime implements CvsSerializer<LocalDateTime> {

    @Override
    public Predicate<Object> getCheck() {
        return fieldValue -> fieldValue instanceof LocalDateTime;
    }

    @Override
    public Function<LocalDateTime, String> getSerialize() {
        return fieldValue -> {
            DateTimeFormatter f = DateTimeFormatter.ofPattern(CSV_DATE_PATTERN);
            ZonedDateTime d = fieldValue.atZone(ZoneId.systemDefault());
            return d.format(f);
        };
    }
}
