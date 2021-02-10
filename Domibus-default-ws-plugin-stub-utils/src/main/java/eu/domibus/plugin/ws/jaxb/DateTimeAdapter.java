package eu.domibus.plugin.ws.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom adapter which extends {@link XmlAdapter} for {@code xsd:dateTime} mapped to {@code LocalDateTime}
 */
public class DateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

    @Override
    public LocalDateTime unmarshal(String s) {
        if (s == null) {
            return null;
        }
        return LocalDateTime.parse(s, DateTimeFormatter.ISO_DATE_TIME);
    }

    @Override
    public String marshal(LocalDateTime dt) {
        if (dt == null) {
            return null;
        }
        return dt.format(DateTimeFormatter.ISO_DATE_TIME);
    }
}