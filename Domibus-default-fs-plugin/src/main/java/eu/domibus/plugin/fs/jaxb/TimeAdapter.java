package eu.domibus.plugin.fs.jaxb;

import eu.domibus.plugin.convert.StringToLocalDateTimeConverter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Custom adapter which extends {@link XmlAdapter} for {@code xsd:time} mapped to {@link LocalTime}
 *
 * @author Cosmin Baciu
 * @since 4.1.2
 */
public class TimeAdapter extends XmlAdapter<String, LocalTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_TIME;

    private final StringToLocalDateTimeConverter converter;

    public TimeAdapter() {
        this.converter = new StringToLocalDateTimeConverter(FORMATTER);
    }

    @Override
    public LocalTime unmarshal(String s) throws Exception {
        return Optional.ofNullable(converter.convert(s))
                .map(localDateTime -> localDateTime.toLocalTime())
                .orElse(null);
    }

    @Override
    public String marshal(LocalTime lt) throws Exception {
        if (lt == null) {
            return null;
        }
        return lt.format(FORMATTER);
    }
}