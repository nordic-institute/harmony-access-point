package eu.domibus.api.model;

import eu.domibus.api.property.DataBaseEngine;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.spring.SpringContextProvider;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.TableGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.LongType;
import org.hibernate.type.Type;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Properties;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.ENGLISH;


/**
 * New sequence format generator. The method generates a new sequence using current date and a fixed length (10 digits) increment.
 *
 * @author François Gautier
 * @since 5.0
 */
public class DatePrefixedGenericSequenceIdGenerator implements DomibusDatePrefixedSequenceIdGeneratorGenerator {

    public static final String DATA_BASE_ENGINE_IS_UNKNOWN = "DataBaseEngine is unknown [";
    public static final String POOLED = "pooled";
    public static final String INITIAL_VALUE = "1000";
    public static final String INC_PARAM = "50";
    public static final String TRUE = "true";
    private final DatePrefixedOracleSequenceIdGenerator datePrefixedOracleSequenceIdGenerator = new DatePrefixedOracleSequenceIdGenerator();
    private final DatePrefixedMysqlSequenceIdGenerator datePrefixedMysqlSequenceIdGenerator = new DatePrefixedMysqlSequenceIdGenerator();
    private DataBaseEngine dataBaseEngine = null;

    @Override
    public void configure(Type type, Properties params,
                          ServiceRegistry serviceRegistry) throws MappingException {
        params.put(TableGenerator.OPT_PARAM, POOLED);
        params.put(TableGenerator.INITIAL_PARAM, INITIAL_VALUE);
        params.put(TableGenerator.INCREMENT_PARAM, INC_PARAM);
        params.put(TableGenerator.CONFIG_PREFER_SEGMENT_PER_ENTITY, TRUE);
        datePrefixedOracleSequenceIdGenerator.configure(LongType.INSTANCE, params, serviceRegistry);
        datePrefixedMysqlSequenceIdGenerator.configure(LongType.INSTANCE, params, serviceRegistry);
    }

    private void initDbEngine() {
        if (dataBaseEngine != null) {
            return;
        }
        ApplicationContext applicationContext = SpringContextProvider.getApplicationContext();
        if (applicationContext == null) {
            return;
        }
        DomibusConfigurationService domibusConfigurationService = applicationContext.getBean(DomibusConfigurationService.class);
        dataBaseEngine = domibusConfigurationService.getDataBaseEngine();
    }

    public DomibusDatePrefixedSequenceIdGeneratorGenerator getGenerator() {
        initDbEngine();
        if (DataBaseEngine.ORACLE == dataBaseEngine) {
            return datePrefixedOracleSequenceIdGenerator;
        }
        if (DataBaseEngine.MYSQL == dataBaseEngine || DataBaseEngine.H2 == dataBaseEngine) {
            return datePrefixedMysqlSequenceIdGenerator;
        }
        throw new IllegalStateException(DATA_BASE_ENGINE_IS_UNKNOWN + dataBaseEngine + "]");
    }

    /**
     * @return id of the shape: yyMMddHHDDDDDDDDDD ex: 210809150000000050
     */
    @Override
    public Serializable generate(SharedSessionContractImplementor session,
                                 Object object) throws HibernateException {
        return getGenerator().generateDomibus(session, object);
    }

    /**
     * @deprecated not used
     */
    @Override
    @Deprecated
    public String[] sqlCreateStrings(Dialect dialect) throws HibernateException {
        return getGenerator().sqlCreateStrings(dialect);
    }

    /**
     * @deprecated not used
     */
    @Override
    @Deprecated
    public String[] sqlDropStrings(Dialect dialect) throws HibernateException {
        return getGenerator().sqlDropStrings(dialect);
    }

    @Override
    public Object generatorKey() {
        return getGenerator().generatorKey();
    }

    @Override
    public void registerExportables(Database database) {
        datePrefixedOracleSequenceIdGenerator.registerExportables(database);
        datePrefixedMysqlSequenceIdGenerator.registerExportables(database);
    }
}
