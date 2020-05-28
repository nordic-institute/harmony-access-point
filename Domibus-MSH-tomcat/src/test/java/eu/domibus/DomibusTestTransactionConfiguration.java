package eu.domibus;

import com.atomikos.icatch.config.UserTransactionServiceImp;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.tomcat.transaction.TomcatTransactionConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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
