package eu.domibus.web.rest;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.multitenancy.*;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.user.ui.User;
import eu.domibus.web.security.UserDetail;
import eu.domibus.core.user.UserPersistenceService;
import eu.domibus.core.user.UserService;
import eu.domibus.core.util.WarningUtil;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.multitenancy.dao.UserDomainDao;
import eu.domibus.web.security.AuthenticationService;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.ChangePasswordRO;
import eu.domibus.web.rest.ro.DomainRO;
import eu.domibus.web.rest.ro.LoginRO;
import eu.domibus.web.rest.ro.UserRO;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@RunWith(JMockit.class)
public class AuthenticationResourceTest {

    @Tested
    AuthenticationResource authenticationResource;

    @Injectable
    AuthenticationService authenticationService;

    @Injectable
    UserDomainDao userDomainDao;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    UserDomainService userDomainService;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    DomainCoreConverter domainCoreConverter;

    @Injectable
    ErrorHandlerService errorHandlerService;

    @Injectable
    protected UserPersistenceService userPersistenceService;

    @Injectable
    private UserService allUserManagementService;

    @Injectable
    private UserService userManagementService;

    @Injectable
    private AuthUtils authUtils;

    @Injectable
    CompositeSessionAuthenticationStrategy compositeSessionAuthenticationStrategy;


    @Mocked
    Logger LOG;

    @Test
    public void testWarningWhenDefaultPasswordUsed(@Mocked WarningUtil warningUtil) throws Exception {
        User user = new User() {{
            setUserName("user");
            setPassword("user");
        }};
        user.setDefaultPassword(true);
        LoginRO loginRO = new LoginRO();
        loginRO.setUsername("user");
        loginRO.setPassword("user");
        final UserDetail userDetail = new UserDetail(user);
        new Expectations() {{
            userDomainService.getDomainForUser(loginRO.getUsername());
            result = DomainService.DEFAULT_DOMAIN.getCode();

            authenticationService.authenticate("user", "user", DomainService.DEFAULT_DOMAIN.getCode());
            result = userDetail;
        }};
        authenticationResource.authenticate(loginRO, new MockHttpServletResponse(), null);
        new Verifications() {{
            String message;
            WarningUtil.warnOutput(message = withCapture());
            assertEquals("user is using default password.", message);
        }};
    }

    @Test
    public void testGetCurrentDomain(@Mocked final LoggerFactory loggerFactory) {
        // Given
        final DomainRO domainRO = new DomainRO();
        domainRO.setCode(DomainService.DEFAULT_DOMAIN.getCode());
        domainRO.setName(DomainService.DEFAULT_DOMAIN.getName());

        new Expectations(authenticationResource) {{
            domainContextProvider.getCurrentDomainSafely();
            result = DomainService.DEFAULT_DOMAIN;

            domainCoreConverter.convert(DomainService.DEFAULT_DOMAIN, DomainRO.class);
            result = domainRO;
        }};

        // When
        final DomainRO result = authenticationResource.getCurrentDomain();

        // Then
        Assert.assertEquals(domainRO, result);
    }

    @Test(expected = DomainTaskException.class)
    public void testExceptionInSetCurrentDomain(@Mocked final LoggerFactory loggerFactory) {
        // Given
        new Expectations(authenticationResource) {{
            authenticationService.changeDomain("");
            result = new DomainTaskException("");
        }};
        // When
        authenticationResource.setCurrentDomain("");
        // Then
        // expect DomainException
    }

    @Test
    public void testChangePassword(@Mocked UserDetail loggedUser, @Mocked ChangePasswordRO changePasswordRO) {

        new Expectations() {{
            authenticationService.getLoggedUser();
            result = loggedUser;

            authUtils.isSuperAdmin();
            result = false;
        }};

        authenticationResource.changePassword(changePasswordRO);

        new Verifications() {{
            userManagementService.changePassword(loggedUser.getUsername(), changePasswordRO.getCurrentPassword(), changePasswordRO.getNewPassword());
            times = 1;
            allUserManagementService.changePassword(loggedUser.getUsername(), changePasswordRO.getCurrentPassword(), changePasswordRO.getNewPassword());
            times = 0;
        }};

        assertEquals(loggedUser.isDefaultPasswordUsed(), false);

    }

    @Test
    public void testLogout_PrincipalExists(final @Mocked HttpServletRequest request, final @Mocked HttpServletResponse response,
                                           final @Mocked SecurityContext securityContext, final @Mocked Authentication authentication,
                                           final @Mocked CookieClearingLogoutHandler cookieClearingLogoutHandler,
                                           final @Mocked SecurityContextLogoutHandler securityContextLogoutHandler) {

        new Expectations(authenticationResource) {{
            new MockUp<SecurityContextHolder>() {
                @Mock
                SecurityContext getContext() {
                    return securityContext;
                }
            };

            securityContext.getAuthentication();
            result = authentication;

            new CookieClearingLogoutHandler("JSESSIONID", "XSRF-TOKEN");
            result = cookieClearingLogoutHandler;

            new SecurityContextLogoutHandler();
            result = securityContextLogoutHandler;


        }};

        //tested method
        authenticationResource.logout(request, response);

        new FullVerifications(authenticationResource) {{
            cookieClearingLogoutHandler.logout(request, response, null);
            securityContextLogoutHandler.logout(request, response, authentication);
        }};
    }

    @Test
    public void testGetUser(final @Mocked UserDetail userDetail) {
        new Expectations() {{
            authenticationService.getLoggedUser();
            result = userDetail;
        }};

        //tested method
        final UserRO userNameActual = authenticationResource.getUser();
        Assert.assertNotNull(userNameActual);
    }

    @Test
    public void testHandleAccountStatusException(final @Mocked AccountStatusException ex) {
        new Expectations(authenticationResource) {{
            errorHandlerService.createResponse(ex, HttpStatus.FORBIDDEN);
            result = any;
        }};
        //tested method
        authenticationResource.handleAccountStatusException(ex);
        new FullVerifications() {{
            errorHandlerService.createResponse(ex, HttpStatus.FORBIDDEN);
            times = 1;
        }};
    }

    @Test
    public void testHandleAuthenticationException(final @Mocked AuthenticationException ex) {
        new Expectations(authenticationResource) {{
            errorHandlerService.createResponse(ex, HttpStatus.FORBIDDEN);
            result = any;
        }};
        //tested method
        authenticationResource.handleAuthenticationException(ex);
        new FullVerifications() {{
            errorHandlerService.createResponse(ex, HttpStatus.FORBIDDEN);
            times = 1;
        }};
    }
}