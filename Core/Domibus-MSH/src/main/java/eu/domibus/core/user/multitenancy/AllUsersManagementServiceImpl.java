package eu.domibus.core.user.multitenancy;

import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.user.User;
import eu.domibus.core.user.UserService;
import eu.domibus.core.user.ui.UserManagementServiceImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Management of all users ( domain and super users), used when a super-user logs in in MT mode
 *
 * @author Ion Perpegel
 * @since 5.0
 */
@Service(AllUsersManagementServiceImpl.BEAN_NAME)
public class AllUsersManagementServiceImpl extends UserManagementServiceImpl {

    public static final String BEAN_NAME = "allUserManagementService";

    private final UserService superUserManagementService;

    private final UserService userManagementService;

    private final UserDomainService userDomainService;

    public AllUsersManagementServiceImpl(@Qualifier(SuperUserManagementServiceImpl.BEAN_NAME) UserService superUserManagementService,
                                         @Qualifier(UserManagementServiceImpl.BEAN_NAME) UserService userManagementService,
                                         UserDomainService userDomainService) {
        this.superUserManagementService = superUserManagementService;
        this.userManagementService = userManagementService;
        this.userDomainService = userDomainService;
    }

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
                .filter(user -> !user.isSuperAdmin())
                .collect(Collectors.toList());
        userManagementService.updateUsers(regularUsers);

        List<eu.domibus.api.user.User> superUsers = users.stream()
                .filter(user -> user.isSuperAdmin())
                .collect(Collectors.toList());
        superUserManagementService.updateUsers(superUsers);
    }

    @Override
    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        boolean isSuper = isSuperUser(username);
        if (isSuper) {
            superUserManagementService.changePassword(username, currentPassword, newPassword);
        } else {
            userManagementService.changePassword(username, currentPassword, newPassword);
        }
    }

    protected boolean isSuperUser(String username) {
        String domain = userDomainService.getDomainForUser(username);
        return domain == null;
    }

}
