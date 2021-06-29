package eu.domibus.api.model;

import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.LongType;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Properties;


/**
 * New sequence format generator. The method generates a new sequence using current date and a fixed length (10 digits) increment.
 *
 * @author idragusa
 * @since 5.0
 */
public class DatePrefixedSequenceIdGenerator extends SequenceStyleGenerator {

    public static final String DATETIME_FORMAT_DEFAULT = "yyMMddHH";
    public static final String NUMBER_FORMAT_DEFAULT = "%010d";

    @Override
    public Serializable generate(SharedSessionContractImplementor session,
                                 Object object) throws HibernateException {

        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        String seqStr = now.format(DateTimeFormatter.ofPattern(DATETIME_FORMAT_DEFAULT, Locale.ENGLISH));

        seqStr += String.format(NUMBER_FORMAT_DEFAULT, super.generate(session, object));;

        return NumberUtils.toLong(seqStr);
    }

    @Override
    public void configure(Type type, Properties params,
                          ServiceRegistry serviceRegistry) throws MappingException {
        super.configure(LongType.INSTANCE, params, serviceRegistry);
    }
}
