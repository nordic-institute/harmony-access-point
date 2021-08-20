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
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * New sequence format generator. The method generates a new sequence using current date and a fixed length (10 digits) incremen
 * @author François Gautier
 * @since 5.0
 */
public interface DomibusDatePrefixedSequenceIdGeneratorGenerator extends IdentifierGenerator, PersistentIdentifierGenerator, Configurable {
    String DATETIME_FORMAT_DEFAULT = "yyMMddHH";
    String NUMBER_FORMAT_DEFAULT = "%010d";

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATETIME_FORMAT_DEFAULT, Locale.ENGLISH);
    ZoneId zoneId = ZoneId.of("UTC");

    /**
     *
     * @return id of the shape: yyMMddHHDDDDDDDDDD ex: 210809150000000050
     *
     */
    default Serializable generateDomibus(SharedSessionContractImplementor session,
                                         Object object) throws HibernateException {

        LocalDateTime now = LocalDateTime.now(zoneId);
        String seqStr = now.format(dtf);

        seqStr += String.format(NUMBER_FORMAT_DEFAULT, this.generate(session, object));;

        return NumberUtils.toLong(seqStr);
    }
}
