package eu.domibus.security;

import eu.domibus.AbstractIT;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.common.JPAConstants;
import eu.domibus.core.user.ui.User;
import eu.domibus.core.user.ui.UserDao;
import eu.domibus.core.user.ui.UserRole;
import eu.domibus.core.user.ui.UserRoleDao;
import eu.domibus.core.user.ui.security.ConsoleUserSecurityPolicyManager;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
public class ConsoleUserSecurityPolicyManagerTestIT extends AbstractIT {

    @Autowired
    ConsoleUserSecurityPolicyManager userSecurityPolicyManager;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    protected UserDao userDao;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    protected EntityManager entityManager;


    private User initTestUser(String userName) {
        UserRole userRole = userRoleDao.findByName("ROLE_USER");
        if (userRole == null) {
            userRole = new UserRole("ROLE_USER");
            entityManager.persist(userRole);
        }
        User user = new User();
        user.setUserName(userName);
        user.setPassword("Password-0");
        user.addRole(userRole);
        user.setEmail("test@mailinator.com");
        user.setActive(true);
        userDao.create(user);
        return user;
    }

    @Test
    @Transactional
    @Rollback
    public void testPasswordReusePolicy_shouldPass() {
        User user = initTestUser("testUser1");
        userSecurityPolicyManager.changePassword(user, "Password-1111111");
        userSecurityPolicyManager.changePassword(user, "Password-2222222");
        userSecurityPolicyManager.changePassword(user, "Password-3333333");
        userSecurityPolicyManager.changePassword(user, "Password-4444444");
        userSecurityPolicyManager.changePassword(user, "Password-5555555");
        userSecurityPolicyManager.changePassword(user, "Password-6666666");
        userSecurityPolicyManager.changePassword(user, "Password-1111111");
    }

    @Test(expected = DomibusCoreException.class)
    @Transactional
    @Rollback
    public void testPasswordReusePolicy_shouldFail() {
        User user = initTestUser("testUser2");
        userSecurityPolicyManager.changePassword(user, "Password-1111111");
        userSecurityPolicyManager.changePassword(user, "Password-2222222");
        userSecurityPolicyManager.changePassword(user, "Password-3333333");
        userSecurityPolicyManager.changePassword(user, "Password-4444444");
        userSecurityPolicyManager.changePassword(user, "Password-5555555");
        userSecurityPolicyManager.changePassword(user, "Password-1111111");
    }

    @Test(expected = DomibusCoreException.class)
    @Transactional
    @Rollback
    public void testPasswordComplexity_blankPasswordShouldFail() {
        User user = initTestUser("testUser3");
        userSecurityPolicyManager.changePassword(user, "");
    }

    @Test(expected = DomibusCoreException.class)
    @Transactional
    @Rollback
    public void testPasswordComplexity_shortPasswordShouldFail() {
        User user = initTestUser("testUser4");
        userSecurityPolicyManager.changePassword(user, "Aa-1");
    }

    @Test(expected = UserManagementException.class)
    @Transactional
    @Rollback
    public void test_validateUniqueUser() {
        User user = initTestUser("testUser_Unique");
        userSecurityPolicyManager.validateUniqueUser(user);
    }
}
