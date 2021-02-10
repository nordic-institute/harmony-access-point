package eu.domibus.plugin.ws.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom adapter which extends {@link XmlAdapter} for {@code xsd:time} mapped to {@link LocalTime}
 */
public class TimeAdapter extends XmlAdapter<String, LocalTime> {

    @Override
    public LocalTime unmarshal(String s) {
        if (s == null) {
            return null;
        }
        return LocalTime.parse(s, DateTimeFormatter.ISO_TIME);
    }

    @Override
    public String marshal(LocalTime lt) {
        if (lt == null) {
            return null;
        }
        return lt.format(DateTimeFormatter.ISO_TIME);
    }
}