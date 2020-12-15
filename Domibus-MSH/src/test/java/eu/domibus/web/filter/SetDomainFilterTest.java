package eu.domibus.web.filter;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.web.security.UserDetail;
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
                         @Mocked FilterChain chain, @Mocked UserDetail userDetail)
            throws IOException, ServletException {

        String domainCode = "default";

        new Expectations(setDomainFilter) {{
            setDomainFilter.getAuthenticatedUser();
            result = userDetail;

            setDomainFilter.getDomain(userDetail);
            this.result = domainCode;
        }};

        setDomainFilter.doFilter(request, response, chain);

        new Verifications() {{
            domainContextProvider.setCurrentDomain(domainCode);
            chain.doFilter(request, response);
        }};
    }

    @Test
    public void getDomain(@Mocked UserDetail userDetail) {

        String domainCode = "default";

        new Expectations(setDomainFilter) {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;

            userDetail.getDomain();
            this.result = domainCode;
        }};

        String result = setDomainFilter.getDomain(userDetail);
        Assert.assertEquals(domainCode, result);
    }
}