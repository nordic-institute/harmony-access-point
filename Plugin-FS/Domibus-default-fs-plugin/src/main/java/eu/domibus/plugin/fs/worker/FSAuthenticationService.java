package eu.domibus.plugin.fs.worker;

import eu.domibus.ext.exceptions.AuthenticationExtException;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.ext.services.AuthenticationExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author Ionut Breaz
 * @since 5.1.8
 */

@Service
public class FSAuthenticationService {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSAuthenticationService.class);

    protected final FSPluginProperties fsPluginProperties;
    protected final DomibusConfigurationExtService domibusConfigurationExtService;
    protected final AuthenticationExtService authenticationExtService;

    public FSAuthenticationService(FSPluginProperties fsPluginProperties,
                                   DomibusConfigurationExtService domibusConfigurationExtService,
                                   AuthenticationExtService authenticationExtService) {
        this.fsPluginProperties = fsPluginProperties;
        this.domibusConfigurationExtService = domibusConfigurationExtService;
        this.authenticationExtService = authenticationExtService;
    }

    public void authenticateForDomain(String domain) throws AuthenticationExtException {
        if (!domibusConfigurationExtService.isSecuredLoginRequired()) {
            LOG.trace("Skip authentication for domain [{}]", domain);
            return;
        }

        String user = fsPluginProperties.getAuthenticationUser(domain);
        if (StringUtils.isBlank(user)) {
            LOG.error("Authentication User not defined for domain [{}]", domain);
            throw new AuthenticationExtException(DomibusErrorCode.DOM_002, "Authentication User not defined for domain [" + domain + "]");
        }

        String password = fsPluginProperties.getAuthenticationPassword(domain);
        if (StringUtils.isBlank(password)) {
            LOG.error("Authentication Password not defined for domain [{}]", domain);
            throw new AuthenticationExtException(DomibusErrorCode.DOM_002, "Authentication Password not defined for domain [" + domain + "]");
        }

        authenticationExtService.basicAuthenticate(user, password);
    }
}
