package eu.domibus.web.security;

import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PASSWORD_POLICY_CHECK_DEFAULT_PASSWORD;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultPasswordInterceptorTest {

    @Tested
    DefaultPasswordInterceptor defaultPasswordInterceptor;
    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;
    @Mocked
    SecurityContextHolder securityContextHolder;
    @Mocked
    Authentication authentication;
    @Mocked
    UserDetail userDetail;

    @Before
    public void setup() {
        new NonStrictExpectations() {{
            domibusPropertyProvider.getProperty(DOMIBUS_PASSWORD_POLICY_CHECK_DEFAULT_PASSWORD);
            result = "true";
            SecurityContextHolder.getContext().getAuthentication();
            result = authentication;
            authentication.isAuthenticated();
            result = true;
            authentication.getPrincipal();
            result = userDetail;
        }};
    }

    @Test
    public void testPreHandleWithDefaultPassword(@Injectable final HttpServletRequest httpRequest,
                                                 @Injectable final HttpServletResponse httpServletResponse,
                                                 @Injectable final Object handler) throws Exception {
        new Expectations() {{
            userDetail.isDefaultPasswordUsed();
            result = true;
        }};

        assertFalse(defaultPasswordInterceptor.preHandle(httpRequest, httpServletResponse, handler));
    }

    @Test
    public void testPreHandleWithChangedPassword(@Injectable final HttpServletRequest httpRequest,
                                                 @Injectable final HttpServletResponse httpServletResponse,
                                                 @Injectable final Object handler ) throws Exception {
        new Expectations() {{
            userDetail.isDefaultPasswordUsed();
            result = false;
        }};

        assertTrue(defaultPasswordInterceptor.preHandle(httpRequest, httpServletResponse, handler));
    }

    @Test
    public void testPreHandleWhenCheckIsDisabled(@Injectable final HttpServletRequest httpRequest,
                                                 @Injectable final HttpServletResponse httpServletResponse,
                                                 @Injectable final Object handler) throws Exception {
        new Expectations() {{
            domibusPropertyProvider.getProperty(DOMIBUS_PASSWORD_POLICY_CHECK_DEFAULT_PASSWORD);
            result = "false";
        }};

        assertTrue(defaultPasswordInterceptor.preHandle(httpRequest, httpServletResponse, handler));
    }

}