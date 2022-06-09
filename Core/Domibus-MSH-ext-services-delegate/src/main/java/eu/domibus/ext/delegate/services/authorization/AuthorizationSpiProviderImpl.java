package eu.domibus.ext.delegate.services.authorization;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.crypto.spi.AuthorizationServiceSpi;
import eu.domibus.core.crypto.spi.model.AuthorizationError;
import eu.domibus.core.crypto.spi.model.AuthorizationException;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EXTENSION_IAM_AUTHORIZATION_IDENTIFIER;

/**
 * @author Thomas Dussart
 * @since 4.2
 */

@Service
public class AuthorizationSpiProviderImpl {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(AuthorizationSpiProviderImpl.class);

    protected static final String IAM_AUTHORIZATION_IDENTIFIER = DOMIBUS_EXTENSION_IAM_AUTHORIZATION_IDENTIFIER;

    protected DomibusPropertyProvider domibusPropertyProvider;

    private List<AuthorizationServiceSpi> authorizationServiceSpis;

    public AuthorizationSpiProviderImpl(DomibusPropertyProvider domibusPropertyProvider, List<AuthorizationServiceSpi> authorizationServiceSpis) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.authorizationServiceSpis = authorizationServiceSpis;
    }

    protected AuthorizationServiceSpi getAuthorizationService() {
        final String authorizationServiceIdentifier = domibusPropertyProvider.getProperty(IAM_AUTHORIZATION_IDENTIFIER);
        final List<AuthorizationServiceSpi> authorizationServiceList = this.authorizationServiceSpis.stream().
                filter(authorizationServiceSpi -> authorizationServiceIdentifier.equals(authorizationServiceSpi.getIdentifier())).
                collect(Collectors.toList());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Authorization spi:");
            authorizationServiceList.forEach(authorizationServiceSpi -> LOG.debug(" identifier:[{}] for class:[{}]", authorizationServiceSpi.getIdentifier(), authorizationServiceSpi.getClass()));
        }

        if (authorizationServiceList.size() > 1) {
            throw new AuthorizationException(AuthorizationError.AUTHORIZATION_MODULE_CONFIGURATION_ISSUE, String.format("More than one authorization service provider for identifier:[%s]", authorizationServiceIdentifier));
        }
        if (authorizationServiceList.isEmpty()) {
            throw new AuthorizationException(AuthorizationError.AUTHORIZATION_MODULE_CONFIGURATION_ISSUE, String.format("No authorisation service provider found for given identifier:[%s]", authorizationServiceIdentifier));
        }
        return authorizationServiceList.get(0);
    }
}
