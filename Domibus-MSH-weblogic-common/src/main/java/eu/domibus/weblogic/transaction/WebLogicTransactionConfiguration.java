package eu.domibus.weblogic.transaction;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.jta.WebLogicJtaTransactionManager;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class WebLogicTransactionConfiguration {

    @Bean("transactionManager")
    public WebLogicJtaTransactionManager webLogicJtaTransactionManager() {
        return new WebLogicJtaTransactionManager();
    }
}
