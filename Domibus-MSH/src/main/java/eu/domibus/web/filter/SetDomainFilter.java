package eu.domibus.web.filter;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.security.UserDetail;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public class SetDomainFilter extends GenericFilterBean {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(SetDomainFilter.class);

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        UserDetail loggedUser = getAuthenticatedUser();
        if (loggedUser != null) {
            String domain = getDomain(loggedUser);
            LOG.debug("Found authenticated user [{}]; setting its domain [{}] on the context.", loggedUser.getUsername(), domain);
            domainContextProvider.setCurrentDomain(domain);
        }
        LOG.debug("No authenticated user found so no domain to set.");

        chain.doFilter(request, response);
    }

    //TODO: replace with an already existing method from AuthenticationServiceBase (or move it in AuthUtils) and reuse everywhere
    // EDELIVERY-7610
    protected UserDetail getAuthenticatedUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && (authentication.getPrincipal() instanceof UserDetail)) {
            return (UserDetail) authentication.getPrincipal();
        }
        return null;
    }

    protected String getDomain(UserDetail user) {
        if (domibusConfigurationService.isMultiTenantAware()) {
            return user.getDomain();
        }
        return DomainService.DEFAULT_DOMAIN.getCode();
    }
}
