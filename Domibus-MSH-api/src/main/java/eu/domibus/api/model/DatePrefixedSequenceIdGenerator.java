package eu.domibus.api.model;

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

public class DatePrefixedSequenceIdGenerator extends SequenceStyleGenerator {

    public static final String DATETIME_FORMAT_DEFAULT = "yyMMddHH";
    public static final String NUMBER_FORMAT_DEFAULT = "%010d";

    @Override
    public Serializable generate(SharedSessionContractImplementor session,
                                 Object object) throws HibernateException {

        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        String seqStr = now.format(DateTimeFormatter.ofPattern(DATETIME_FORMAT_DEFAULT, Locale.ENGLISH));

        seqStr += String.format(NUMBER_FORMAT_DEFAULT, super.generate(session, object));;

        long id = new Long(seqStr);

        return id;
    }

    @Override
    public void configure(Type type, Properties params,
                          ServiceRegistry serviceRegistry) throws MappingException {
        super.configure(LongType.INSTANCE, params, serviceRegistry);
    }
}
