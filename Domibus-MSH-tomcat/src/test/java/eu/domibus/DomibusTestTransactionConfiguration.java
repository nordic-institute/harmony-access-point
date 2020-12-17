package eu.domibus;

import java.util.Properties;

import com.atomikos.icatch.config.UserTransactionServiceImp;
import eu.domibus.api.property.DomibusPropertyProvider;
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

    @Override
    @Primary
    @Bean(value = "userTransactionService", initMethod = "init", destroyMethod = "shutdownForce")
    public UserTransactionServiceImp userTransactionServiceImp(DomibusPropertyProvider domibusPropertyProvider) {
        return super.userTransactionServiceImp(domibusPropertyProvider);
    }

    @Override
    protected Properties getAtomikosProperties(DomibusPropertyProvider domibusPropertyProvider) {
        Properties atomikosProperties = super.getAtomikosProperties(domibusPropertyProvider);
        atomikosProperties.setProperty("com.atomikos.icatch.enable_logging", "none");
        atomikosProperties.setProperty("com.atomikos.icatch.oltp_max_retries", "0");
        return atomikosProperties;
    }
}
