package eu.domibus.web.rest;

import com.google.common.collect.ImmutableMap;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.api.user.UserState;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.user.plugin.AuthenticationEntity;
import eu.domibus.core.user.plugin.PluginUserService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.ErrorRO;
import eu.domibus.web.rest.ro.PluginUserFilterRequestRO;
import eu.domibus.web.rest.ro.PluginUserRO;
import eu.domibus.web.rest.ro.PluginUserResultRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
@RestController
@RequestMapping(value = "/rest/plugin")
@Validated
public class PluginUserResource extends BaseResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserResource.class);

    @Autowired
    private PluginUserService pluginUserService;

    @Autowired
    private DomainCoreConverter domainConverter;

    @Autowired
    private ErrorHandlerService errorHandlerService;

    @Autowired
    protected UserDomainService userDomainService;

    @ExceptionHandler({UserManagementException.class})
    public ResponseEntity<ErrorRO> handleUserManagementException(UserManagementException ex) {
        return errorHandlerService.createResponse(ex, HttpStatus.CONFLICT);
    }

    @GetMapping(value = {"/users"})
    public PluginUserResultRO findUsers(PluginUserFilterRequestRO request) {
        PluginUserResultRO result = retrieveAndPackageUsers(request);
        Long count = pluginUserService.countUsers(request.getAuthType(), request.getAuthRole(), request.getOriginalUser(), request.getUserName());
        result.setCount(count);
        return result;
    }

    @PutMapping(value = {"/users"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateUsers(@RequestBody @Valid List<PluginUserRO> userROs) {
        LOG.debug("Update plugin users was called: {}", userROs);

        List<PluginUserRO> addedUsersRO = userROs.stream().filter(u -> UserState.NEW.name().equals(u.getStatus())).collect(Collectors.toList());
        List<PluginUserRO> updatedUsersRO = userROs.stream().filter(u -> UserState.UPDATED.name().equals(u.getStatus())).collect(Collectors.toList());
        List<PluginUserRO> removedUsersRO = userROs.stream().filter(u -> UserState.REMOVED.name().equals(u.getStatus())).collect(Collectors.toList());

        List<AuthenticationEntity> addedUsers = domainConverter.convert(addedUsersRO, AuthenticationEntity.class);
        List<AuthenticationEntity> updatedUsers = domainConverter.convert(updatedUsersRO, AuthenticationEntity.class);
        List<AuthenticationEntity> removedUsers = domainConverter.convert(removedUsersRO, AuthenticationEntity.class);

        pluginUserService.updateUsers(addedUsers, updatedUsers, removedUsers);
    }

    /**
     * This method returns a CSV file with the contents of Plugin User table
     *
     * @return CSV file with the contents of Plugin User table
     */
    @GetMapping(path = "/csv")
    public ResponseEntity<String> getCsv(PluginUserFilterRequestRO request) {
        request.setPageStart(0);
        request.setPageSize(getCsvService().getPageSizeForExport());
        final PluginUserResultRO result = retrieveAndPackageUsers(request);
        getCsvService().validateMaxRows(result.getEntries().size(),
                () -> pluginUserService.countUsers(request.getAuthType(), request.getAuthRole(), request.getOriginalUser(), request.getUserName()));

        return exportToCSV(result.getEntries(),
                PluginUserRO.class,
                ImmutableMap.of(
                        "UserName".toUpperCase(), "Username",
                        "authRoles".toUpperCase(), "Role"
                ),
                Arrays.asList("entityId", "status", "password", "domain"),
                "pluginusers");
    }

    protected PluginUserResultRO retrieveAndPackageUsers(PluginUserFilterRequestRO request) {
        LOG.debug("Retrieving plugin users.");
        List<PluginUserRO> users = pluginUserService.findUsers(request.getAuthType(), request.getAuthRole(), request.getOriginalUser(), request.getUserName(),
                request.getPageStart(), request.getPageSize());

        PluginUserResultRO result = new PluginUserResultRO();
        result.setEntries(users);
        result.setPage(request.getPageStart());
        result.setPageSize(request.getPageSize());

        return result;
    }

}
