package eu.domibus.web.filter;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.web.security.DomibusUserDetails;
import mockit.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@RunWith(MockitoJUnitRunner.class)
public class SetDomainFilterTest {

    @Tested
    SetDomainFilter setDomainFilter;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Test
    public void doFilter(@Mocked ServletRequest request, @Mocked ServletResponse response,
                         @Mocked FilterChain chain, @Mocked DomibusUserDetails domibusUserDetails)
            throws IOException, ServletException {

        String domainCode = "default";

        new Expectations(setDomainFilter) {{
            setDomainFilter.getAuthenticatedUser();
            result = domibusUserDetails;

            setDomainFilter.getDomain(domibusUserDetails);
            this.result = domainCode;
        }};

        setDomainFilter.doFilter(request, response, chain);

        new Verifications() {{
            domainContextProvider.setCurrentDomain(domainCode);
            chain.doFilter(request, response);
        }};
    }

    @Test
    public void getDomain(@Mocked DomibusUserDetails domibusUserDetails) {

        String domainCode = "default";

        new Expectations(setDomainFilter) {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;

            domibusUserDetails.getDomain();
            this.result = domainCode;
        }};

        String result = setDomainFilter.getDomain(domibusUserDetails);
        Assert.assertEquals(domainCode, result);
    }
}