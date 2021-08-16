package eu.domibus.api.model;

import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


/**
 * New sequence format generator. The method generates a new sequence using current date and a fixed length (10 digits) increment.
 *
 * @author idragusa
 * @since 5.0
 */
public class DatePrefixedMysqlSequenceIdGenerator extends org.hibernate.id.enhanced.TableGenerator {

    public static final String DATETIME_FORMAT_DEFAULT = "yyMMddHH";
    public static final String NUMBER_FORMAT_DEFAULT = "%010d";

    final DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATETIME_FORMAT_DEFAULT, Locale.ENGLISH);
    final ZoneId zoneId = ZoneId.of("UTC");

    /**
     *
     * @return id of the shape: yyMMddHHDDDDDDDDDD ex: 210809150000000050
     *
     */
    @Override
    public Serializable generate(SharedSessionContractImplementor session,
                                 Object object) throws HibernateException {

        LocalDateTime now = LocalDateTime.now(zoneId);
        String seqStr = now.format(dtf);

        seqStr += String.format(NUMBER_FORMAT_DEFAULT, super.generate(session, object));;

        return NumberUtils.toLong(seqStr);
    }
}
