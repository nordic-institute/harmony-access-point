package eu.domibus.api.model;

import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.PersistentIdentifierGenerator;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.ENGLISH;

/**
 * New sequence format generator. The method generates a new sequence using current date and a fixed length (10 digits) incremen
 *
 * @author François Gautier
 * @since 5.0
 */
public interface DomibusDatePrefixedSequenceIdGeneratorGenerator extends IdentifierGenerator, PersistentIdentifierGenerator, Configurable {

    String DATETIME_FORMAT_DEFAULT = "yyMMddHH";
    long MAX_DATETIME_NUMBER = 99999999L;

    String MAX = "9999999999";
    long MAX_INCREMENT_NUMBER = 9999999999L;

    String MIN = "0000000000";

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATETIME_FORMAT_DEFAULT, Locale.ENGLISH);

    ZoneId zoneId = ZoneId.of("UTC");

    /**
     * @return id of the shape: yyMMddHHDDDDDDDDDD ex: 210809150000000050
     */
    default Serializable generateDomibus(SharedSessionContractImplementor session,
                                         Object object) throws HibernateException {
        LocalDateTime now = getCurrentDate();
        String seqStr = now.format(dtf);
        String paddedSequence = MIN + this.generate(session, object);
        // add 10 right digits to the date
        seqStr += paddedSequence.substring(paddedSequence.length() - MIN.length());
        return NumberUtils.createLong(seqStr);
    }

    default LocalDateTime getCurrentDate() {
        return LocalDateTime.now(zoneId);
    }
}
