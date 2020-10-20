package eu.domibus.core.user.ui;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.core.alerts.service.ConsoleUserAlertsServiceImpl;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.dao.ListDao;
import eu.domibus.core.user.UserLoginErrorReason;
import eu.domibus.core.user.UserPersistenceService;
import eu.domibus.core.user.UserService;
import eu.domibus.core.user.ui.converters.UserConverter;
import eu.domibus.core.user.ui.security.ConsoleUserSecurityPolicyManager;
import eu.domibus.core.user.ui.security.password.ConsoleUserPasswordHistoryDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

/**
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
    @Qualifier("userFilteringDao")
    private ListDao listDao;

    @Autowired
    private DomainCoreConverter domainConverter;

    public static final String ALL_USERS = "all";


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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserLoginErrorReason handleWrongAuthentication(final String userName) {
        return userPasswordManager.handleWrongAuthentication(userName);
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

    public void validateAtLeastOneOfRole(AuthRole role) {
        List<User> users = userDao.findByRole(role.toString());
        long count = users.stream().filter(u -> !u.isDeleted() && u.isActive()).count();
        if (count == 0) {
            throw new UserManagementException("There must always be at least one active Domain Admin for each Domain.");
        }
    }

    @Override
    public List<eu.domibus.api.user.User> findUsers(AuthRole authRole, String userName, String deleted, int page, int pageSize) {
        Map<String, Object> filters = createFilterMap(authRole, userName, deleted);
        List<User> users = listDao.findPaged(page * pageSize, pageSize, "entityId", true, filters);
        List<eu.domibus.api.user.User> finalUsers = prepareUsers(this::getDomainForUser, users);

        return finalUsers;
    }

    @Override
    public long countUsers(AuthRole authRole, String userName, String deleted) {
        Map<String, Object> filters = createFilterMap(authRole,userName, deleted);
         List<User> users = userDao.listUsers();
        return users.size();
    }

    protected Map<String, Object> createFilterMap(AuthRole authRole, String userName, String deleted) {
        HashMap<String, Object> filters = new HashMap<>();
        if (authRole != null) {
            UserRole userRole = userRoleDao.findByName(authRole.name());
            Set<UserRole> roles = new HashSet<>();
            roles.add(userRole);
            filters.put("roles", roles);
        }
        if (deleted.equals(ALL_USERS)) {
            filters.put("deleted", null);

        } else {
            filters.put("deleted", Boolean.parseBoolean(deleted));
        }
        filters.put("userName", userName);

        LOG.debug("Added users filters: [{}]", filters);
        return filters;
    }
}
