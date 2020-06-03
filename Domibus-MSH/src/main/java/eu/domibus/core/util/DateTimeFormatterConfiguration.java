package eu.domibus.core.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

/**
 * @author Cosmin Baciu
 * @since 4.1.2
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

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss[.SSS][z]";

    @Bean
    public DateTimeFormatter dateTimeFormatter() {
        return DateTimeFormatter.ofPattern(DATE_FORMAT);
    }

    @Bean("xmlDateTimeFormat")
    public SimpleDateFormat simpleDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    }
}
