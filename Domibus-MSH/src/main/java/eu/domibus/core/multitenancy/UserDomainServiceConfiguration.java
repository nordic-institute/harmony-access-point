package eu.domibus.core.multitenancy;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Ion Perpegel(nperpion)
 * @since 4.0
 */
@Configuration
public class UserDomainServiceConfiguration {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(UserDomainServiceConfiguration.class);

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Bean
    public UserDomainService userDomainService () {
        if (domibusConfigurationService.isMultiTenantAware()) {
            LOG.debug("Instantiating multi-tenancy variant of UserDomainService");
            return new UserDomainServiceMultiDomainImpl();
        } else {
            LOG.debug("Instantiating non multi-tenancy variant of UserDomainService");
            return new UserDomainServiceSingleDomainImpl();
        }
    }
}
