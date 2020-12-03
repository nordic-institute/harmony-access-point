package eu.domibus.core.user.multitenancy;

import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.user.User;
import eu.domibus.core.multitenancy.dao.UserDomainDao;
import eu.domibus.core.multitenancy.dao.UserDomainEntity;
import eu.domibus.core.user.ui.UserManagementServiceImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Management of all users ( domain and super users), used when a super-user logs in in MT mode
 *
 * @author Ion Perpegel
 * @since 4.0
 */
@Service(SuperUserManagementServiceImpl.BEAN_NAME)
public class SuperUserManagementServiceImpl extends UserManagementServiceImpl {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SuperUserManagementServiceImpl.class);

    public static final String BEAN_NAME = "superUserManagementService";

    @Autowired
    protected UserDomainService userDomainService;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @Autowired
    protected UserDomainDao userDomainDao;

    @Autowired
    protected UserManagementServiceImpl userManagementService;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<eu.domibus.api.user.User> findUsers() {
        // retrieve domain users
        List<eu.domibus.api.user.User> allUsers = userManagementService.findUsers();

        // retrieve super users
        List<eu.domibus.api.user.User> superUsers = getSuperUsers();
        allUsers.addAll(superUsers);

        return allUsers;
    }

    /**
     * Search users based on the following filters.
     *
     * @param authRole criteria to search the role of user (ROLE_ADMIN or ROLE_USER)
     * @param userName criteria to search by userName
     * @param page     pagination start
     * @param pageSize page size.
     */
    @Override
    public List<eu.domibus.api.user.User> findUsersWithFilters(AuthRole authRole, String userName, String deleted, int page, int pageSize) {
        // retrieve domain users
        List<eu.domibus.api.user.User> allUsers = userManagementService.findUsersWithFilters(authRole, userName, deleted, page, pageSize);

        // retrieve super users
        List<eu.domibus.api.user.User> superUsers = getSuperUsersWithFilters(authRole, userName, deleted, page, pageSize);
        allUsers.addAll(superUsers);

        return allUsers;
    }

    /**
     * Get super users from the general schema with the filters. <br>
     * This is done in a separate thread as the DB connection is cached per thread and cannot be changed anymore to the schema of the associated domain
     *
     * @param authRole criteria to search the role of user (ROLE_ADMIN or ROLE_USER)
     * @param userName criteria to search by userName
     * @param page     pagination start
     * @param pageSize page size
     * @return the list of users from the general schema
     */
    protected List<User> getSuperUsersWithFilters(AuthRole authRole, String userName, String deleted, int page, int pageSize) {
        LOG.debug("Searching for super users");
        return domainTaskExecutor.submit(() -> super.findUsersWithFilters(authRole, userName, deleted, page, pageSize, this::getPreferredDomainForUser));
    }


    /**
     * Get all super users from the general schema. <br>
     * This is done in a separate thread as the DB connection is cached per thread and cannot be changed anymore to the schema of the associated domain
     *
     * @return the list of users from the general schema
     */
    protected List<User> getSuperUsers() {
        LOG.debug("Searching for super users");
        return domainTaskExecutor.submit(() -> super.findUsers(this::getPreferredDomainForUser));
    }

    protected String getPreferredDomainForUser(eu.domibus.api.user.User user) {
        List<UserDomainEntity> domains = userDomainDao.listPreferredDomains();
        String domainCode = domains.stream()
                .filter(domainEntity -> domainEntity.getUserName().equals(user.getUserName()))
                .map(domainEntity -> domainEntity.getPreferredDomain())
                .findFirst()
                .orElse(null);
        return domainCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateUsers(List<eu.domibus.api.user.User> users) {

        // cannot reuse the bellow code properly because of the use of super keyword
        // TODO: refactor this class by splitting it in 2: SuperUserManagementService( deals only with super users)
        // and AllUserManagementService which is the aggregator of SuperUserManagementService and UserManagementService
        // EDELIVERY-7510
        List<eu.domibus.api.user.User> regularUsers = users.stream()
                .filter(u -> !u.getAuthorities().contains(AuthRole.ROLE_AP_ADMIN.name()))
                .collect(Collectors.toList());
        try {
            userManagementService.updateUsers(regularUsers);
        } catch (DomibusCoreException ex) {
            LOG.trace("Remove domain association for new users.");
            regularUsers.stream()
                    .filter(user -> user.isNew())
                    .forEach(user -> userDomainService.deleteDomainForUser(user.getUserName()));
            throw ex;
        }

        List<eu.domibus.api.user.User> superUsers = users.stream()
                .filter(u -> u.getAuthorities().contains(AuthRole.ROLE_AP_ADMIN.name()))
                .collect(Collectors.toList());

        // TODO: better add a new method on domainTaskExecutor: submitWithSecurityContext that preserves the sec context
        // another solution would be to keep the security context/ principal in a service of ours
        final Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
        domainTaskExecutor.submit(() -> {
            // we need the security context because we try to get the logged user down the way
            SecurityContextHolder.getContext().setAuthentication(currentAuthentication);
            try {
                super.updateUsers(superUsers);
            } catch (DomibusCoreException ex) {
                LOG.trace("Remove domain association for new super users.");
                superUsers.stream()
                        .filter(user -> user.isNew())
                        .forEach(user -> userDomainService.deleteDomainForUser(user.getUserName()));
                throw ex;
            }
        });
    }

    @Override
    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        domainTaskExecutor.submit(() -> {
            super.changePassword(username, currentPassword, newPassword);
        });
    }

    protected AuthRole getAdminRole() {
        return AuthRole.ROLE_AP_ADMIN;
    }
}
