package eu.domibus.tomcat.transaction;

import com.atomikos.icatch.config.UserTransactionServiceImp;
import com.atomikos.icatch.jta.J2eeUserTransaction;
import com.atomikos.icatch.jta.UserTransactionManager;

import eu.domibus.common.JMSConstants;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.jpa.DomibusJPAConfiguration;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.transaction.jta.JtaTransactionManager;

import java.util.Properties;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class TomcatTransactionConfiguration {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(TomcatTransactionConfiguration.class);

    @Bean(value = "userTransactionService", initMethod = "init", destroyMethod = "shutdownWait")
    public UserTransactionServiceImp userTransactionServiceImp(DomibusPropertyProvider domibusPropertyProvider) {
        Properties properties = getAtomikosProperties(domibusPropertyProvider);

        LOGGER.debug("Configured UserTransactionService with the following properties [{}]", properties);

        return new UserTransactionServiceImp(properties);
    }

    protected Properties getAtomikosProperties(DomibusPropertyProvider domibusPropertyProvider) {
        Properties properties = new Properties();
        properties.setProperty("com.atomikos.icatch.service", "com.atomikos.icatch.standalone.UserTransactionServiceFactory");

        String icatchOutputDirectory = domibusPropertyProvider.getProperty(DomibusPropertyMetadataManagerSPI.COM_ATOMIKOS_ICATCH_OUTPUT_DIR);
        properties.setProperty("com.atomikos.icatch.output_dir", icatchOutputDirectory);

        String logBaseDirectory = domibusPropertyProvider.getProperty(DomibusPropertyMetadataManagerSPI.COM_ATOMIKOS_ICATCH_LOG_BASE_DIR);
        properties.setProperty("com.atomikos.icatch.log_base_dir", logBaseDirectory);

        properties.setProperty("com.atomikos.icatch.force_shutdown_on_vm_exit", "true");

        String defaultJtaTimeout = domibusPropertyProvider.getProperty(DomibusPropertyMetadataManagerSPI.COM_ATOMIKOS_ICATCH_DEFAULT_JTA_TIMEOUT);
        properties.setProperty("com.atomikos.icatch.default_jta_timeout", defaultJtaTimeout);

        String maxTimeout = domibusPropertyProvider.getProperty(DomibusPropertyMetadataManagerSPI.COM_ATOMIKOS_ICATCH_MAX_TIMEOUT);
        properties.setProperty("com.atomikos.icatch.max_timeout", maxTimeout);

        String maxActives = domibusPropertyProvider.getProperty(DomibusPropertyMetadataManagerSPI.COM_ATOMIKOS_ICATCH_MAX_ACTIVES);
        properties.setProperty("com.atomikos.icatch.max_actives", maxActives);
        return properties;
    }

    @DependsOn("userTransactionService")
    @Bean(value = "atomikosTransactionManager", initMethod = "init", destroyMethod = "close")
    public UserTransactionManager userTransactionManager() {
        UserTransactionManager result = new UserTransactionManager();
        result.setForceShutdown(false);
        result.setStartupTransactionService(false);
        return result;
    }

    @DependsOn("userTransactionService")
    @Bean("atomikosUserTransaction")
    public J2eeUserTransaction j2eeUserTransaction() {
        return new J2eeUserTransaction();
    }

    @DependsOn({JMSConstants.DOMIBUS_JMS_XACONNECTION_FACTORY, JMSConstants.DOMIBUS_JMS_CACHING_XACONNECTION_FACTORY,
                DomibusJPAConfiguration.DOMIBUS_JDBC_XA_DATA_SOURCE, DomibusJPAConfiguration.DOMIBUS_JDBC_NON_XA_DATA_SOURCE})
    @Bean("transactionManager")
    public JtaTransactionManager jtaTransactionManager(@Qualifier("atomikosTransactionManager") UserTransactionManager userTransactionManager,
                                                       @Qualifier("atomikosUserTransaction") J2eeUserTransaction j2eeUserTransaction) {
        JtaTransactionManager result = new JtaTransactionManager();
        result.setTransactionManager(userTransactionManager);
        result.setUserTransaction(j2eeUserTransaction);
        result.setAllowCustomIsolationLevels(true);
        return result;
    }
}
