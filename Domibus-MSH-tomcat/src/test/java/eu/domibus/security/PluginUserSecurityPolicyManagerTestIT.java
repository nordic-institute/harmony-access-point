package eu.domibus.security;

import eu.domibus.AbstractIT;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.core.alerts.service.PluginUserAlertsServiceImpl;
import eu.domibus.core.user.plugin.AuthenticationDAO;
import eu.domibus.core.user.plugin.AuthenticationEntity;
import eu.domibus.core.user.plugin.security.PluginUserSecurityPolicyManager;
import eu.domibus.core.user.plugin.security.password.PluginUserPasswordHistoryDao;
import eu.domibus.core.user.ui.UserRole;
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
public class PluginUserSecurityPolicyManagerTestIT extends AbstractIT {

    @Autowired
    PluginUserSecurityPolicyManager userSecurityPolicyManager;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected AuthenticationDAO userDao;

    @Autowired
    private PluginUserPasswordHistoryDao userPasswordHistoryDao;

    @Autowired
    private PluginUserAlertsServiceImpl userAlertsService;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @PersistenceContext(unitName = "domibusJTA")
    protected EntityManager entityManager;


    private AuthenticationEntity initTestUser(String userName) {
        UserRole userRole = entityManager.find(UserRole.class, 1L);
        if (userRole == null) {
            userRole = new UserRole("ROLE_USER");
            entityManager.persist(userRole);
        }
        AuthenticationEntity user = new AuthenticationEntity();
        user.setUserName(userName);
        user.setPassword("Password-0");
        user.setAuthRoles("ROLE_USER");
        user.setActive(true);
        userDao.create(user);
        return user;
    }

    @Test
    @Transactional
    @Rollback
    public void testPasswordReusePolicy_shouldPass() {
        AuthenticationEntity user = initTestUser("testUser1");
        userSecurityPolicyManager.changePassword(user, "Password-1");
        userSecurityPolicyManager.changePassword(user, "Password-2");
        userSecurityPolicyManager.changePassword(user, "Password-3");
        userSecurityPolicyManager.changePassword(user, "Password-4");
        userSecurityPolicyManager.changePassword(user, "Password-5");
        userSecurityPolicyManager.changePassword(user, "Password-6");
        userSecurityPolicyManager.changePassword(user, "Password-1");
    }

    @Test(expected = DomibusCoreException.class)
    @Transactional
    @Rollback
    public void testPasswordReusePolicy_shouldFail() {
        AuthenticationEntity user = initTestUser("testUser2");
        userSecurityPolicyManager.changePassword(user, "Password-1");
        userSecurityPolicyManager.changePassword(user, "Password-2");
        userSecurityPolicyManager.changePassword(user, "Password-3");
        userSecurityPolicyManager.changePassword(user, "Password-4");
        userSecurityPolicyManager.changePassword(user, "Password-5");
        userSecurityPolicyManager.changePassword(user, "Password-1");
    }

    @Test(expected = DomibusCoreException.class)
    @Transactional
    @Rollback
    public void testPasswordComplexity_blankPasswordShouldFail() {
        AuthenticationEntity user = initTestUser("testUser3");
        userSecurityPolicyManager.changePassword(user, "");
    }

    @Test(expected = DomibusCoreException.class)
    @Transactional
    @Rollback
    public void testPasswordComplexity_shortPasswordShouldFail() {
        AuthenticationEntity user = initTestUser("testUser4");
        userSecurityPolicyManager.changePassword(user, "Aa-1");
    }

    @Test(expected = UserManagementException.class)
    @Transactional
    @Rollback
    public void test_validateUniqueUser() {
        AuthenticationEntity user = initTestUser("testUser_Unique");
        userSecurityPolicyManager.validateUniqueUser(user);
    }
}