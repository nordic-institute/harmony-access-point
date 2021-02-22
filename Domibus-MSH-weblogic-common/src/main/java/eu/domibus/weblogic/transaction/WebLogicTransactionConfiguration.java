package eu.domibus.weblogic.transaction;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.WebLogicJtaTransactionManager;

import javax.persistence.EntityManagerFactory;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class WebLogicTransactionConfiguration {

    @Bean("transactionManager")
    @DependsOn("entityManagerFactory")
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
