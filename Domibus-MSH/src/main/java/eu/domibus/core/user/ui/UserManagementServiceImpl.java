package eu.domibus.core.user.ui;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.user.AtLeastOneAdminException;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.core.alerts.service.ConsoleUserAlertsServiceImpl;
import eu.domibus.core.user.UserLoginErrorReason;
import eu.domibus.core.user.UserPersistenceService;
import eu.domibus.core.user.UserService;
import eu.domibus.core.user.ui.converters.UserConverter;
import eu.domibus.core.user.ui.security.ConsoleUserSecurityPolicyManager;
import eu.domibus.core.user.ui.security.password.ConsoleUserPasswordHistoryDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * * Management of regular users, used in ST mode and when a domain admin user logs in in MT mode
 *
 * @author Thomas Dussart, Ion Perpegel
 * @since 3.3
 */
@Service(UserManagementServiceImpl.BEAN_NAME)
public class UserManagementServiceImpl implements UserService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserManagementServiceImpl.class);

    public static final String BEAN_NAME = "userManagementService";

    @Autowired
    protected UserDao userDao;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    protected ConsoleUserPasswordHistoryDao userPasswordHistoryDao;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected UserConverter userConverter;

    @Autowired
    protected UserPersistenceService userPersistenceService;

    @Autowired
    protected UserDomainService userDomainService;

    @Autowired
    protected DomainService domainService;

    @Autowired
    ConsoleUserSecurityPolicyManager userPasswordManager;

    @Autowired
    ConsoleUserAlertsServiceImpl userAlertsService;

    @Autowired
    protected AuthUtils authUtils;

    @Autowired
    private UserFilteringDao listDao;

    private static final String ALL_USERS = "all";
    private static final String USER_NAME = "userName";
    private static final String USER_ROLE = "userRole";
    private static final String DELETED_USER = "deleted";

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<eu.domibus.api.user.User> findUsers() {
        return findUsers(this::getDomainForUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<eu.domibus.api.user.UserRole> findUserRoles() {
        LOG.debug("Retrieving user roles");
        List<UserRole> userRolesEntities = userRoleDao.listRoles();

        List<eu.domibus.api.user.UserRole> userRoles = new ArrayList<>();
        for (UserRole userRoleEntity : userRolesEntities) {
            eu.domibus.api.user.UserRole userRole = new eu.domibus.api.user.UserRole(userRoleEntity.getName());
            userRoles.add(userRole);
        }
        return userRoles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateUsers(List<eu.domibus.api.user.User> users) {
        userPersistenceService.updateUsers(users);
        ensureAtLeastOneActiveAdmin();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized UserLoginErrorReason handleWrongAuthentication(final String userName) {
        // there is no security context when the user failed to login -> we're creating one
        return authUtils.runFunctionWithDomibusSecurityContext(() -> userPasswordManager.handleWrongAuthentication(userName), AuthRole.ROLE_ADMIN, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void reactivateSuspendedUsers() {
        userPasswordManager.reactivateSuspendedUsers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCorrectAuthentication(final String userName) {
        userPasswordManager.handleCorrectAuthentication(userName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateExpiredPassword(final String userName) {
        User user = getUserWithName(userName);
        boolean defaultPassword = user.hasDefaultPassword();
        LocalDateTime passwordChangeDate = user.getPasswordChangeDate();

        userPasswordManager.validatePasswordExpired(userName, defaultPassword, passwordChangeDate);
    }

    @Override
    public Integer getDaysTillExpiration(String userName) {
        User user = getUserWithName(userName);
        boolean isDefaultPassword = user.hasDefaultPassword();
        LocalDateTime passwordChangeDate = user.getPasswordChangeDate();

        return userPasswordManager.getDaysTillExpiration(userName, isDefaultPassword, passwordChangeDate);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void triggerPasswordAlerts() {
        userAlertsService.triggerPasswordExpirationEvents();
    }

    @Override
    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        userPersistenceService.changePassword(username, currentPassword, newPassword);
    }

    /**
     * Retrieves users from DB and sets some attributes for each user
     *
     * @param getDomainForUserFn the function to get the domain
     * @return the list of users
     */
    protected List<eu.domibus.api.user.User> findUsers(Function<eu.domibus.api.user.User, String> getDomainForUserFn) {
        LOG.debug("Retrieving console users");
        List<User> userEntities = userDao.listUsers();

        return prepareUsers(getDomainForUserFn, userEntities);
    }

    /**
     * Calls a function to get the domain for each user and also sets expiration date
     *
     * @param getDomainForUserFn the function to get the domain
     * @return the list of users
     */
    protected List<eu.domibus.api.user.User> prepareUsers(Function<eu.domibus.api.user.User, String> getDomainForUserFn, List<User> userEntities) {
        List<eu.domibus.api.user.User> users = new ArrayList<>();
        userEntities.forEach(userEntity -> {
            eu.domibus.api.user.User user = convertAndPrepareUser(getDomainForUserFn, userEntity);
            users.add(user);
        });
        return users;
    }

    protected eu.domibus.api.user.User convertAndPrepareUser(Function<eu.domibus.api.user.User, String> getDomainForUserFn, User userEntity) {
        eu.domibus.api.user.User user = userConverter.convert(userEntity);

        String domainCode = getDomainForUserFn.apply(user);
        user.setDomain(domainCode);

        LocalDateTime expDate = userPasswordManager.getExpirationDate(userEntity);
        user.setExpirationDate(expDate);
        return user;
    }

    private String getDomainForUser(eu.domibus.api.user.User user) {
        return userDomainService.getDomainForUser(user.getUserName());
    }

    protected User getUserWithName(String userName) {
        User user = userDao.findByUserName(userName);
        if (user == null) {
            throw new UserManagementException("Could not find console user with the name " + userName);
        }
        return user;
    }

    protected void ensureAtLeastOneActiveAdmin() {
        AuthRole role = getAdminRole();
        List<User> users = userDao.findByRole(role.toString());
        long count = users.stream().filter(u -> !u.isDeleted() && u.isActive()).count();
        if (count == 0) {
            throw new AtLeastOneAdminException();
        }
    }

    /**
     * Search users based on the following criteria's.
     *
     * @param authRole criteria to search the role of user (ROLE_ADMIN or ROLE_USER)
     * @param userName criteria to search by userName
     * @param page     pagination start
     * @param pageSize page size.
     */
    @Override
    public List<eu.domibus.api.user.User> findUsersWithFilters(AuthRole authRole, String userName, String deleted, int page, int pageSize) {
        return findUsersWithFilters(authRole, userName, deleted, page, pageSize, this::getDomainForUser);
    }


    protected List<eu.domibus.api.user.User> findUsersWithFilters(AuthRole authRole, String userName, String deleted, int page, int pageSize, Function<eu.domibus.api.user.User, String> getDomainForUserFn) {

        LOG.debug("Retrieving console users");
        Map<String, Object> filters = createFilterMap(userName, deleted, authRole);
        List<User> users = listDao.findPaged(page * pageSize, pageSize, "entityId", true, filters);
        List<eu.domibus.api.user.User> finalUsers = prepareUsers(getDomainForUserFn, users);
        return finalUsers;
    }


    @Override
    public long countUsers(AuthRole authRole, String userName, String deleted) {
        Map<String, Object> filters = createFilterMap(userName, deleted, authRole);
        return listDao.countEntries(filters);
    }

    protected Map<String, Object> createFilterMap(String userName, String deleted, AuthRole authRole) {
        HashMap<String, Object> filters = new HashMap<>();
        addUserNameFilter(userName, filters);
        addDeletedUserFilter(deleted, filters);
        addUserRoleFilter(authRole, filters);
        LOG.debug("Added users filters: [{}]", filters);
        return filters;
    }

    protected void addUserRoleFilter(AuthRole authRole, HashMap<String, Object> filters) {
        if (authRole != null) {
            filters.put(USER_ROLE, authRole.name());
        }
    }

    protected void addUserNameFilter(String userName, HashMap<String, Object> filters) {
        if (userName != null) {
            filters.put(USER_NAME, userName);
        }
    }

    protected void addDeletedUserFilter(String deleted, HashMap<String, Object> filters) {
        if (StringUtils.equals(deleted, ALL_USERS)) {
            filters.put(DELETED_USER, null);
        } else {
            filters.put(DELETED_USER, Boolean.parseBoolean(deleted));
        }
    }

    protected AuthRole getAdminRole() {
        return AuthRole.ROLE_ADMIN;
    }
}
