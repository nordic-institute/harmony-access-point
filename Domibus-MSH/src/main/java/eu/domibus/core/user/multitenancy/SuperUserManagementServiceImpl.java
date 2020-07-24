package eu.domibus.core.user.multitenancy;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<eu.domibus.api.user.User> findUsers() {
        // retrieve domain users
        List<eu.domibus.api.user.User> allUsers = super.findUsers();

        // retrieve super users
        List<eu.domibus.api.user.User> superUsers = getSuperUsers();
        allUsers.addAll(superUsers);

        return allUsers;
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
        List<eu.domibus.api.user.User> regularUsers = users.stream()
                .filter(u -> !u.getAuthorities().contains(AuthRole.ROLE_AP_ADMIN.name()))
                .collect(Collectors.toList());
        super.updateUsers(regularUsers);
        super.validateAtLeastOneOfRole(AuthRole.ROLE_ADMIN);

        List<eu.domibus.api.user.User> superUsers = users.stream()
                .filter(u -> u.getAuthorities().contains(AuthRole.ROLE_AP_ADMIN.name()))
                .collect(Collectors.toList());

        domainTaskExecutor.submit(() -> {
            // this block needs to called inside a transaction;
            // for this the whole code inside the block needs to reside into a Spring bean service marked with transaction REQUIRED
            super.updateUsers(superUsers);
            super.validateAtLeastOneOfRole(AuthRole.ROLE_AP_ADMIN);
        });
    }

    @Override
    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        domainTaskExecutor.submit(() -> {
            super.changePassword(username, currentPassword, newPassword);
        });
    }

}
