package eu.domibus.plugin.ws.jaxb;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.convert.StringToTemporalAccessorConverter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * Custom adapter which extends {@link XmlAdapter} for {@code xsd:time} mapped to {@link LocalTime}
 */
public class TimeAdapter extends XmlAdapter<String, LocalTime> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TimeAdapter.class);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_TIME;

    private final StringToTemporalAccessorConverter converter;

    public TimeAdapter() {
        this.converter = new StringToTemporalAccessorConverter(FORMATTER);
    }

    @Override
    public LocalTime unmarshal(String s) throws Exception {
        TemporalAccessor converted = converter.convert(s);
        if(!(converted instanceof LocalTime)) {
            LOG.warn("The source [{}] could not be correctly converted to a local time instance [{}]", s, converted);
            return null;
        }
        return (LocalTime) converter.convert(s);
    }

    @Override
    public String marshal(LocalTime lt) {
        if (lt == null) {
            LOG.info("Returning null value for a null local time input");
            return null;
        }
        return lt.format(FORMATTER);
    }
}