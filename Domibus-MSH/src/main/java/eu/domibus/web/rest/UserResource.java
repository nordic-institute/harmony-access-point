package eu.domibus.web.rest;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.DomainTaskException;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.user.User;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.api.user.UserRole;
import eu.domibus.api.user.UserState;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.user.UserService;
import eu.domibus.core.user.multitenancy.SuperUserManagementServiceImpl;
import eu.domibus.core.user.ui.UserManagementServiceImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.ErrorRO;
import eu.domibus.web.rest.ro.UserFilterRequestRO;
import eu.domibus.web.rest.ro.UserResponseRO;
import eu.domibus.web.rest.ro.UserResultRO;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/user")
@Validated
public class UserResource extends BaseResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserResource.class);

    @Autowired
    @Lazy
    @Qualifier(SuperUserManagementServiceImpl.BEAN_NAME)
    private UserService superUserManagementService;

    @Autowired
    @Lazy
    @Qualifier(UserManagementServiceImpl.BEAN_NAME)
    private UserService userManagementService;

    @Autowired
    private DomainCoreConverter domainConverter;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private ErrorHandlerService errorHandlerService;

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    @ExceptionHandler({UserManagementException.class})
    public ResponseEntity<ErrorRO> handleUserManagementException(UserManagementException ex) {
        return errorHandlerService.createResponse(ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({DomainTaskException.class})
    public ResponseEntity<ErrorRO> handleDomainException(DomainTaskException ex) {
        //We caught it here just to check for UserManagementException and put HttpStatus.CONFLICT;  otherwise we would have delegated to general error handler
        Throwable rootException = ExceptionUtils.getRootCause(ex);
        if (rootException instanceof UserManagementException) {
            return handleUserManagementException((UserManagementException) rootException);
        }
        return errorHandlerService.createResponse(ex);
    }

    /**
     * {@inheritDoc}
     */
    @GetMapping(value = {"/users"})
    public List<UserResponseRO> getUsers() {
        LOG.debug("Retrieving users");

        List<User> users = getUserService().findUsers();

        return prepareResponse(users);
    }

    @PutMapping(value = {"/users"})
    public void updateUsers(@RequestBody @Valid List<UserResponseRO> userROS) {
        LOG.debug("Update Users was called: {}", userROS);
        validateUsers(userROS);
        updateUserRoles(userROS);
        List<User> users = domainConverter.convert(userROS, User.class);
        getUserService().updateUsers(users);
    }

    @GetMapping(value = {"/userroles"})
    public List<String> userRoles() {
        List<String> result = new ArrayList<>();
        List<UserRole> userRoles = getUserService().findUserRoles();
        for (UserRole userRole : userRoles) {
            result.add(userRole.getRole());
        }

        // ROLE_AP_ADMIN role is available only to superusers
        if (authUtils.isSuperAdmin()) {
            result.add(AuthRole.ROLE_AP_ADMIN.name());
        }

        return result;
    }

    /**
     * This method returns a CSV file with the contents of User table
     *
     * @return CSV file with the contents of User table
     */
    @GetMapping(path = "/csv")
    public ResponseEntity<String> getCsv(UserFilterRequestRO request) {
        request.setPageStart(0);
        request.setPageSize(getCsvService().getPageSizeForExport());
        final UserResultRO result = retrieveAndPackageUsers(request);

        getCsvService().validateMaxRows(result.getEntries().size(),
                () -> getUserService().countUsers(request.getAuthRole(), request.getUserName(), request.getDeleted()));


        return exportToCSV(result.getEntries(),
                UserResponseRO.class,
                ImmutableMap.of(
                        "UserName".toUpperCase(), "Username",
                        "Roles".toUpperCase(), "Role"
                ),
                domibusConfigurationService.isMultiTenantAware() ?
                        Arrays.asList("authorities", "status", "password", "suspended") :
                        Arrays.asList("authorities", "status", "password", "suspended", "domain"),
                "users");
    }

    private UserService getUserService() {
        if (authUtils.isSuperAdmin()) {
            return superUserManagementService;
        } else {
            return userManagementService;
        }
    }

    protected UserResultRO retrieveAndPackageUsers(UserFilterRequestRO request) {
        LOG.debug("Retrieving users.");
        List<User> users = getUserService().findUsersWithFilters(request.getAuthRole(), request.getUserName(), request.getDeleted(),
                request.getPageStart(), request.getPageSize());
        List<UserResponseRO> userResponseROS = prepareResponse(users);
        UserResultRO result = new UserResultRO();
        result.setEntries(userResponseROS);
        result.setPage(request.getPageStart());
        result.setPageSize(request.getPageSize());

        return result;
    }


    private void validateUsers(List<UserResponseRO> users) {
        users.forEach(user -> {
            if (Strings.isNullOrEmpty(user.getUserName())) {
                throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "User name cannot be null.");
            }

            if (Strings.isNullOrEmpty(user.getRoles())) {
                throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "User role cannot be null.");
            }
        });
    }

    private void updateUserRoles(List<UserResponseRO> userROS) {
        for (UserResponseRO userRo : userROS) {
            if (Objects.equals(userRo.getStatus(), UserState.NEW.name()) || Objects.equals(userRo.getStatus(), UserState.UPDATED.name())) {
                List<String> auths = Arrays.asList(userRo.getRoles().split(","));
                userRo.setAuthorities(auths);
            }
        }
    }

    /**
     * convert User to UserResponseRO.
     *
     * @param users
     * @return a list of
     */
    protected List<UserResponseRO> prepareResponse(List<User> users) {
        List<UserResponseRO> userResponseROS = domainConverter.convert(users, UserResponseRO.class);
        for (UserResponseRO userResponseRO : userResponseROS) {
            userResponseRO.setStatus("PERSISTED");
            userResponseRO.updateRolesField();
        }
        return userResponseROS;

    }

}
