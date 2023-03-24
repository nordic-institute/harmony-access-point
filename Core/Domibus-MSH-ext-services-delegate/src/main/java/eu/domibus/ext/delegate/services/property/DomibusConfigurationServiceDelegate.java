package eu.domibus.ext.delegate.services.property;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Tiago Miguel
 * @since 4.0
 */
@Service
public class DomibusConfigurationServiceDelegate implements DomibusConfigurationExtService {

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Autowired
    protected AuthUtils authUtils;

    @Override
    public boolean isMultiTenantAware() {
        return domibusConfigurationService.isMultiTenantAware();
    }

    @Override
    public boolean isSingleTenantAware() {
        return domibusConfigurationService.isSingleTenantAware();
    }

    @Override
    public boolean isSecuredLoginRequired() {
        return !authUtils.isUnsecureLoginAllowed();
    }

    @Override
    public String getConfigLocation() {
        return domibusConfigurationService.getConfigLocation();
    }

    @Override
    public boolean isClusterDeployment() {
        return domibusConfigurationService.isClusterDeployment();
    }
}
