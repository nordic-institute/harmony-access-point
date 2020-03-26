package eu.domibus;

import com.atomikos.icatch.config.UserTransactionServiceImp;
import com.atomikos.icatch.jta.J2eeUserTransaction;
import com.atomikos.icatch.jta.UserTransactionManager;
import eu.domibus.api.property.DomibusPropertyManager;
import eu.domibus.api.property.DomibusPropertyMetadataManager;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.tomcat.transaction.TomcatTransactionConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.jta.JtaTransactionManager;

import java.util.Properties;

/**
 * @author Ioana Dragusanu
 * @since 4.2
 */
@Configuration
public class DomibusTestTransactionConfiguration extends TomcatTransactionConfiguration {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusTestTransactionConfiguration.class);

    @Override
    @Primary
    @Bean(value = "userTransactionService", initMethod = "init", destroyMethod = "shutdownForce")
    public UserTransactionServiceImp userTransactionServiceImp(DomibusPropertyProvider domibusPropertyProvider) {
        return super.userTransactionServiceImp(domibusPropertyProvider);
    }
}
