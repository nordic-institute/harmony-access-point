package eu.domibus.core.user.multitenancy;

import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.user.AtLeastOneAdminException;
import eu.domibus.api.user.User;
import eu.domibus.core.multitenancy.dao.UserDomainDao;
import eu.domibus.core.multitenancy.dao.UserDomainEntity;
import eu.domibus.core.user.UserService;
import eu.domibus.core.user.ui.UserManagementServiceImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Management of all users ( domain and super users), used when a super-user logs in in MT mode
 *
 * @author Ion Perpegel
 * @since 4.0
 */
@Service(AllUserManagementServiceImpl.BEAN_NAME)
public class AllUserManagementServiceImpl extends UserManagementServiceImpl {

    public static final String BEAN_NAME = "allUserManagementService";

    @Autowired
    @Qualifier(SuperUserManagementServiceImpl.BEAN_NAME)
    private UserService superUserManagementService;

    @Autowired
    @Qualifier(UserManagementServiceImpl.BEAN_NAME)
    private UserService userManagementService;

    @Autowired
    protected UserDomainService userDomainService;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> findUsers() {
        List<User> allUsers = new ArrayList<>();

        // retrieve domain users
        List<User> regularUsers = userManagementService.findUsers();
        allUsers.addAll(regularUsers);

        // retrieve super users
        List<User> superUsers = superUserManagementService.findUsers();
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
    public List<User> findUsersWithFilters(AuthRole authRole, String userName, String deleted, int page, int pageSize) {
        List<User> allUsers = new ArrayList<>();

        // retrieve domain users
        List<User> regularUsers = userManagementService.findUsersWithFilters(authRole, userName, deleted, page, pageSize);
        allUsers.addAll(regularUsers);

        // retrieve super users
        List<User> superUsers = superUserManagementService.findUsersWithFilters(authRole, userName, deleted, page, pageSize);
        allUsers.addAll(superUsers);

        return allUsers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateUsers(List<User> users) {
        List<eu.domibus.api.user.User> regularUsers = users.stream()
                .filter(u -> !u.getAuthorities().contains(AuthRole.ROLE_AP_ADMIN.name()))
                .collect(Collectors.toList());
        userManagementService.updateUsers(regularUsers);

        List<eu.domibus.api.user.User> superUsers = users.stream()
                .filter(u -> u.getAuthorities().contains(AuthRole.ROLE_AP_ADMIN.name()))
                .collect(Collectors.toList());
        superUserManagementService.updateUsers(superUsers);
    }

    @Override
    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        String domain = userDomainService.getDomainForUser(username);
        if(domain == null) {
            superUserManagementService.changePassword(username, currentPassword, newPassword);
        } else {
            userManagementService.changePassword(username, currentPassword, newPassword);
        }
    }

}
