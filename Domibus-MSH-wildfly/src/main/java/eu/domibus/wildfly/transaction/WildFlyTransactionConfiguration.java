package eu.domibus.wildfly.transaction;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.jta.JtaTransactionManager;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class WildFlyTransactionConfiguration {

    @Bean("transactionManager")
    public JtaTransactionManager jtaTransactionManager() {
        return new JtaTransactionManager();
    }
}
