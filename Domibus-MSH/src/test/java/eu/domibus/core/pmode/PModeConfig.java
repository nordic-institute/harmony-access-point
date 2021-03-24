package eu.domibus.core.pmode;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author François Gautier
 * @since 5.0
 */
@Configuration
public class PModeConfig {

    @Bean
    public ConfigurationRawDAO getConfigurationRawDAO() {
        return new ConfigurationRawDAO();
    }
    @Bean
    public ConfigurationRawTestService ConfigurationRawServiceTest() {
        return new ConfigurationRawTestService();
    }

}
