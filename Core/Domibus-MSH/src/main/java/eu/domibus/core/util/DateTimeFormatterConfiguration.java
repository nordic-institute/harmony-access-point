package eu.domibus.core.util;

import eu.domibus.api.property.DomibusPropertyProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATE_TIME_PATTERN_ON_RECEIVING;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATE_TIME_PATTERN_ON_SENDING;

/**
 * @author Cosmin Baciu
 * @author François Gautier
 * @since 4.2
 * <p>
 * Example of valid dateTime:
 * 2020-06-02T20:00:00
 * 2020-06-02T20:00:00Z
 * 2020-06-02T09:00:00.000Z
 * 2020-06-02T23:00:00.000+03:00
 * 2020-06-02T23:00:00+03:00
 * <p>
 * Specifications: https://www.w3.org/TR/2004/REC-xmlschema-2-20041028/datatypes.html#dateTime
 * The ·lexical space· of dateTime consists of finite-length sequences of characters of the form:
 * '-'? yyyy '-' mm '-' dd 'T' hh ':' mm ':' ss ('.' s+)? (zzzzzz)?
 */
@Configuration
public class DateTimeFormatterConfiguration {

    protected final DomibusPropertyProvider domibusPropertyProvider;

    public DateTimeFormatterConfiguration(DomibusPropertyProvider domibusPropertyProvider) {
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    @Bean
    public DateTimeFormatter dateTimeFormatter() {
        return DateTimeFormatter.ofPattern(domibusPropertyProvider.getProperty(DOMIBUS_DATE_TIME_PATTERN_ON_RECEIVING));
    }

    @Bean("xmlDateTimeFormat")
    public SimpleDateFormat simpleDateFormat() {
        return new SimpleDateFormat(domibusPropertyProvider.getProperty(DOMIBUS_DATE_TIME_PATTERN_ON_SENDING));
    }
}
