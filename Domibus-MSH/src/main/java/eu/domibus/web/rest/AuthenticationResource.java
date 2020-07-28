package eu.domibus.web.rest;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.user.UserService;
import eu.domibus.core.user.multitenancy.SuperUserManagementServiceImpl;
import eu.domibus.core.user.ui.UserManagementServiceImpl;
import eu.domibus.core.util.WarningUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.*;
import eu.domibus.web.security.AuthenticationService;
import eu.domibus.web.security.DomibusCookieClearingLogoutHandler;
import eu.domibus.web.security.UserDetail;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Cosmin Baciu, Catalin Enache
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/security")
@Validated
public class AuthenticationResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuthenticationResource.class);

    @Autowired
    protected AuthenticationService authenticationService;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected UserDomainService userDomainService;

    @Autowired
    protected DomainCoreConverter domainCoreConverter;

    @Autowired
    protected ErrorHandlerService errorHandlerService;

    @Autowired
    @Lazy
    @Qualifier(SuperUserManagementServiceImpl.BEAN_NAME)
    private UserService superUserManagementService;

    @Autowired
    @Lazy
    @Qualifier(UserManagementServiceImpl.BEAN_NAME)
    private UserService userManagementService;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    CompositeSessionAuthenticationStrategy sas;


    @ExceptionHandler({AccountStatusException.class})
    public ResponseEntity<ErrorRO> handleAccountStatusException(AccountStatusException ex) {
        return errorHandlerService.createResponse(ex, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<ErrorRO> handleAuthenticationException(AuthenticationException ex) {
        return errorHandlerService.createResponse(ex, HttpStatus.FORBIDDEN);
    }

    @RequestMapping(value = "authentication", method = RequestMethod.POST)
    @Transactional(noRollbackFor = BadCredentialsException.class)
    public UserRO authenticate(@RequestBody @Valid LoginRO loginRO, HttpServletResponse response, HttpServletRequest request) {

        String domainCode = userDomainService.getDomainForUser(loginRO.getUsername());
        LOG.debug("Determined domain [{}] for user [{}]", domainCode, loginRO.getUsername());

        if (StringUtils.isNotBlank(domainCode)) {   //domain user
            domainContextProvider.setCurrentDomain(domainCode);
        } else {                    //ap user
            domainContextProvider.clearCurrentDomain();
            domainCode = userDomainService.getPreferredDomainForUser(loginRO.getUsername());
            if (StringUtils.isBlank(domainCode)) {
                LOG.securityInfo(DomibusMessageCode.SEC_CONSOLE_LOGIN_UNKNOWN_USER, loginRO.getUsername());
                throw new BadCredentialsException("The username/password combination you provided are not valid. Please try again or contact your administrator.");
            }

            LOG.debug("Determined preferred domain [{}] for user [{}]", domainCode, loginRO.getUsername());
        }

        LOG.debug("Authenticating user [{}]", loginRO.getUsername());
        final UserDetail principal = authenticationService.authenticate(loginRO.getUsername(), loginRO.getPassword(), domainCode);
        if (principal.isDefaultPasswordUsed()) {
            LOG.warn(WarningUtil.warnOutput(principal.getUsername() + " is using default password."));
        }

        sas.onAuthentication( SecurityContextHolder.getContext().getAuthentication(), request, response);

        return createUserRO(principal, loginRO.getUsername());
    }

    @RequestMapping(value = "authentication", method = RequestMethod.DELETE)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            LOG.debug("Cannot perform logout: no user is authenticated");
            return;
        }

        LOG.debug("Logging out user [" + auth.getName() + "]");
        new DomibusCookieClearingLogoutHandler("JSESSIONID", "XSRF-TOKEN").logout(request, response, null);
        LOG.debug("Cleared cookies");
        new SecurityContextLogoutHandler().logout(request, response, auth);
        LOG.debug("Logged out");
    }

    @RequestMapping(value = "username", method = RequestMethod.GET)
    public String getUsername() {
        return Optional.ofNullable(getLoggedUser()).map(UserDetail::getUsername).orElse(StringUtils.EMPTY);
    }

    @RequestMapping(value = "user", method = RequestMethod.GET)
    public UserRO getUser() {
        LOG.debug("get user - start");
        UserDetail userDetail = getLoggedUser();

        return userDetail != null ? createUserRO(userDetail, userDetail.getUsername()) : null;
    }

    /**
     * Retrieve the current domain of the current user (in multi-tenancy mode)
     *
     * @return the current domain
     */
    @RequestMapping(value = "user/domain", method = RequestMethod.GET)
    public DomainRO getCurrentDomain() {
        LOG.debug("Getting current domain");
        Domain domain = domainContextProvider.getCurrentDomainSafely();
        return domainCoreConverter.convert(domain, DomainRO.class);
    }

    /**
     * Set the current domain of the current user (in multi-tenancy mode)
     *
     * @param domainCode the code of the new current domain
     */
    @RequestMapping(value = "user/domain", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setCurrentDomain(@RequestBody @Valid String domainCode) {
        LOG.debug("Setting current domain " + domainCode);
        authenticationService.changeDomain(domainCode);
    }

    /**
     * Set the password of the current user
     *
     * @param param the object holding the current and new passwords of the current user
     *
     * */
    @RequestMapping(value = "user/password", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestBody @Valid ChangePasswordRO param) {
        UserDetail loggedUser = this.getLoggedUser();
        LOG.debug("Changing password for user [{}]", loggedUser.getUsername());
        getUserService().changePassword(loggedUser.getUsername(), param.getCurrentPassword(), param.getNewPassword());
        loggedUser.setDefaultPasswordUsed(false);
    }

    /**
     * It will return the Principal from {@link SecurityContextHolder}
     * if different from {@link AnonymousAuthenticationToken}
     * @return
     */
    UserDetail getLoggedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication!= null && !(authentication instanceof AnonymousAuthenticationToken)) {
            UserDetail userDetail = (UserDetail) authentication.getPrincipal();
            LOG.debug("Principal found on SecurityContextHolder: {}", userDetail);
            return userDetail;
        }
        return null;
    }

    UserService getUserService() {
        if (authUtils.isSuperAdmin()) {
            return superUserManagementService;
        } else {
            return userManagementService;
        }
    }


    private UserRO createUserRO(UserDetail principal, String username) {
        //Parse Granted authorities to a list of string authorities
        List<String> authorities = new ArrayList<>();
        for (GrantedAuthority grantedAuthority : principal.getAuthorities()) {
            authorities.add(grantedAuthority.getAuthority());
        }

        UserRO userRO = new UserRO();
        userRO.setUsername(username);
        userRO.setAuthorities(authorities);
        userRO.setDefaultPasswordUsed(principal.isDefaultPasswordUsed());
        userRO.setDaysTillExpiration(principal.getDaysTillExpiration());
        userRO.setExternalAuthProvider(principal.isExternalAuthProvider());
        return userRO;
    }

}
