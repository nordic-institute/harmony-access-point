package eu.domibus.plugin.ws.jaxb;

import eu.domibus.plugin.convert.StringToLocalDateTimeConverter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Custom adapter which extends {@link XmlAdapter} for {@code xsd:date} mapped to {@link LocalDate}
 */
public class DateAdapter extends XmlAdapter<String, LocalDate> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE;

    private final StringToLocalDateTimeConverter converter;

    public DateAdapter() {
        this.converter = new StringToLocalDateTimeConverter(FORMATTER);
    }

    @Override
    public LocalDate unmarshal(String s) throws Exception {
        //this is mapped to xsd:date with or without timezone
        return Optional.ofNullable(converter.convert(s))
                .map(localDateTime -> localDateTime.toLocalDate())
                .orElse(null);
    }

    @Override
    public String marshal(LocalDate dt) throws Exception {
        if (dt == null) {
            return null;
        }
        return dt.format(FORMATTER);
    }

}