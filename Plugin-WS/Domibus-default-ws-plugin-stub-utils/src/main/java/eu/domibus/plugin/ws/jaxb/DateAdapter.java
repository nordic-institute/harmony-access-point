package eu.domibus.plugin.ws.jaxb;

import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.convert.StringToTemporalAccessorConverter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * Custom adapter which extends {@link XmlAdapter} for {@code xsd:date} mapped to {@link LocalDate}
 */
public class DateAdapter extends XmlAdapter<String, LocalDate> {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(DateAdapter.class);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE;

    private final StringToTemporalAccessorConverter converter;

    public DateAdapter() {
        this.converter = new StringToTemporalAccessorConverter(FORMATTER);
    }

    @Override
    public LocalDate unmarshal(String s) throws Exception {
        //this is mapped to xsd:date with or without timezone
        TemporalAccessor converted = converter.convert(s);
        if(!(converted instanceof LocalDate)) {
            LOG.warn("The source [{}] could not be correctly converted to a local date instance [{}]", s, converted);
            return null;
        }
        return (LocalDate) converted;
    }

    @Override
    public String marshal(LocalDate dt) throws Exception {
        if (dt == null) {
            LOG.info("Returning null value for a null local date input");
            return null;
        }
        return dt.format(FORMATTER);
    }

}