package eu.domibus.api.model;

import eu.domibus.api.property.DataBaseEngine;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.spring.SpringContextProvider;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.LongType;
import org.hibernate.type.Type;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.util.Properties;


/**
 * New sequence format generator. The method generates a new sequence using current date and a fixed length (10 digits) increment.
 *
 * @author idragusa
 * @since 5.0
 */
public class DatePrefixedGenericSequenceIdGenerator implements PersistentIdentifierGenerator, Configurable {

    public static final String DATA_BASE_ENGINE_IS_UNKNOWN = "DataBaseEngine is unknown [";
    private final DatePrefixedOracleSequenceIdGenerator datePrefixedOracleSequenceIdGenerator = new DatePrefixedOracleSequenceIdGenerator();
    private final DatePrefixedMysqlSequenceIdGenerator datePrefixedMysqlSequenceIdGenerator = new DatePrefixedMysqlSequenceIdGenerator();
    private DataBaseEngine dataBaseEngine = null;

    @Override
    public void configure(Type type, Properties params,
                          ServiceRegistry serviceRegistry) throws MappingException {
        params.put("optimizer", "pooled");
        params.put("initial_value", "1000");
        params.put("increment_size", "50");
        params.put("prefer_entity_table_as_segment_value", "true");
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

    /**
     * @return id of the shape: yyMMddHHDDDDDDDDDD ex: 210809150000000050
     */
    @Override
    public Serializable generate(SharedSessionContractImplementor session,
                                 Object object) throws HibernateException {
        initDbEngine();

        if (DataBaseEngine.ORACLE == dataBaseEngine) {
            return datePrefixedOracleSequenceIdGenerator.generate(session, object);
        }
        if (DataBaseEngine.MYSQL == dataBaseEngine) {
            return datePrefixedMysqlSequenceIdGenerator.generate(session, object);
        }
        throw new IllegalStateException(DATA_BASE_ENGINE_IS_UNKNOWN + dataBaseEngine + "]");
    }


    @Override
    public String[] sqlCreateStrings(Dialect dialect) throws HibernateException {
        initDbEngine();

        if (DataBaseEngine.ORACLE == dataBaseEngine) {
            return datePrefixedOracleSequenceIdGenerator.sqlCreateStrings(dialect);
        }
        if (DataBaseEngine.MYSQL == dataBaseEngine) {
            return datePrefixedMysqlSequenceIdGenerator.sqlCreateStrings(dialect);
        }
        throw new IllegalStateException(DATA_BASE_ENGINE_IS_UNKNOWN + dataBaseEngine + "]");

    }

    @Override
    public String[] sqlDropStrings(Dialect dialect) throws HibernateException {
        initDbEngine();

        if (DataBaseEngine.ORACLE == dataBaseEngine) {
            return datePrefixedOracleSequenceIdGenerator.sqlDropStrings(dialect);
        }
        if (DataBaseEngine.MYSQL == dataBaseEngine) {
            return datePrefixedMysqlSequenceIdGenerator.sqlDropStrings(dialect);
        }
        throw new IllegalStateException(DATA_BASE_ENGINE_IS_UNKNOWN + dataBaseEngine + "]");

    }

    @Override
    public Object generatorKey() {
        initDbEngine();

        if (DataBaseEngine.ORACLE == dataBaseEngine) {
            return datePrefixedOracleSequenceIdGenerator.generatorKey();
        }
        if (DataBaseEngine.MYSQL == dataBaseEngine) {
            return datePrefixedMysqlSequenceIdGenerator.generatorKey();
        }
        throw new IllegalStateException(DATA_BASE_ENGINE_IS_UNKNOWN + dataBaseEngine + "]");

    }

    @Override
    public void registerExportables(Database database) {
        datePrefixedOracleSequenceIdGenerator.registerExportables(database);
        datePrefixedMysqlSequenceIdGenerator.registerExportables(database);
    }
}
