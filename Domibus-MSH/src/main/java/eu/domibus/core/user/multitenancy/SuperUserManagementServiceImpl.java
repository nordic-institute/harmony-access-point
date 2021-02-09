package eu.domibus.core.user.multitenancy;

import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.user.AtLeastOneAdminException;
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
@Service (SuperUserManagementServiceImpl.BEAN_NAME)
public class SuperUserManagementServiceImpl extends UserManagementServiceImpl {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SuperUserManagementServiceImpl.class);

    public static final String BEAN_NAME = "superUserManagementService";

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @Autowired
    protected UserDomainDao userDomainDao;

    /**
     * Get all super users from the general schema. <br>
     * This is done in a separate thread as the DB connection is cached per thread and cannot be changed anymore to the schema of the associated domain
     *
     * @return the list of users from the general schema
     */
    @Override
    public List<eu.domibus.api.user.User> findUsers() {
        LOG.debug("Searching for super users");
        return domainTaskExecutor.submit(() -> super.findUsers(this::getPreferredDomainForUser));
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
    @Override
    public List<eu.domibus.api.user.User> findUsersWithFilters(AuthRole authRole, String userName, String deleted, int page, int pageSize) {
        LOG.debug("Searching for super users");
        return domainTaskExecutor.submit(() -> super.findUsersWithFilters(authRole, userName, deleted, page, pageSize, this::getPreferredDomainForUser));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateUsers(List<eu.domibus.api.user.User> users) {
        // TODO: maybe add a new method on domainTaskExecutor: submitWithSecurityContext that preserves the sec context
        final Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
        domainTaskExecutor.submit(() -> {
            // we need the security context restored on this thread because we try to get the logged user down the way
            SecurityContextHolder.getContext().setAuthentication(currentAuthentication);
            super.updateUsers(users);
        });
    }

    @Override
    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        domainTaskExecutor.submit(() -> {
            super.changePassword(username, currentPassword, newPassword);
        });
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

    protected AuthRole getAdminRole() {
        return AuthRole.ROLE_AP_ADMIN;
    }
}
