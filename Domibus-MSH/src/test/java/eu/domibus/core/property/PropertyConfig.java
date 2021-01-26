package eu.domibus.core.property;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordDecryptionService;
import eu.domibus.core.property.encryption.PasswordDecryptionServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

/**
 * @author François Gautier
 * @since 5.0
 */
@Configuration
public class PropertyConfig {
    @Bean
    public DomibusPropertyProvider domibusPropertyProvider(GlobalPropertyMetadataManager globalPropertyMetadataManager,
                                                           PropertyProviderDispatcher propertyProviderDispatcher,
                                                           PrimitivePropertyTypesManager primitivePropertyTypesManager,
                                                           NestedPropertiesManager nestedPropertiesManager,
                                                           ConfigurableEnvironment environment,
                                                           PropertyProviderHelper propertyProviderHelper,
                                                           PasswordDecryptionService passwordDecryptionService) {
        return new DomibusPropertyProviderImpl(globalPropertyMetadataManager,
                propertyProviderDispatcher,
                primitivePropertyTypesManager,
                nestedPropertiesManager,
                environment,
                propertyProviderHelper,
                passwordDecryptionService);
    }

    @Bean
    public GlobalPropertyMetadataManager globalPropertyMetadataManager() {
        return new GlobalPropertyMetadataManagerImpl(null, null, null, null);
    }

    @Bean
    public PropertyProviderDispatcher propertyProviderDispatcher(PrimitivePropertyTypesManager primitivePropertyTypesManager,
                                                                 PropertyProviderHelper propertyProviderHelper) {
        return new PropertyProviderDispatcher(null, null, null, null, primitivePropertyTypesManager, propertyProviderHelper);
    }

    @Bean
    public PrimitivePropertyTypesManager primitivePropertyTypesManager() {
        return new PrimitivePropertyTypesManager(null);
    }

    @Bean
    public NestedPropertiesManager nestedPropertiesManager() {
        return new NestedPropertiesManager(null, null);
    }

    @Bean
    public ConfigurableEnvironment environment() {
        return new StandardEnvironment();
    }

    @Bean
    public PropertyProviderHelper propertyProviderHelper(ConfigurableEnvironment environment) {
        return new PropertyProviderHelper(environment);
    }

    @Bean
    public PasswordDecryptionService passwordDecryptionService() {
        return new PasswordDecryptionServiceImpl(null, null, null, null);
    }
}




