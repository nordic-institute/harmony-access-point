package eu.domibus.plugin.ws.jaxb;

import eu.domibus.plugin.convert.StringToLocalDateTimeConverter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom adapter which extends {@link XmlAdapter} for {@code xsd:dateTime} mapped to {@code LocalDateTime}
 */
public class DateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private final StringToLocalDateTimeConverter converter;

    public DateTimeAdapter() {
        this.converter = new StringToLocalDateTimeConverter(FORMATTER);
    }

    @Override
    public LocalDateTime unmarshal(String s) {
        return converter.convert(s);
    }

    @Override
    public String marshal(LocalDateTime dt) {
        if (dt == null) {
            return null;
        }
        return dt.format(FORMATTER);
    }
}