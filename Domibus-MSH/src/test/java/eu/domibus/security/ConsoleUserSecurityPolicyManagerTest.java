package eu.domibus.security;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.common.dao.security.ConsoleUserPasswordHistoryDao;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserRole;
import eu.domibus.core.alerts.service.ConsoleUserAlertsServiceImpl;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static eu.domibus.security.ConsoleUserSecurityPolicyManager.LOGIN_SUSPENSION_TIME;
import static eu.domibus.security.ConsoleUserSecurityPolicyManager.MAXIMUM_LOGIN_ATTEMPT;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

public class ConsoleUserSecurityPolicyManagerTest {

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    ConsoleUserPasswordHistoryDao userPasswordHistoryDao;

    @Injectable
    UserDao userDao;

    @Injectable
    BCryptPasswordEncoder bcryptEncoder;

    @Injectable
    ConsoleUserAlertsServiceImpl userAlertsService;

    @Injectable
    UserDomainService userDomainService;

    @Injectable
    DomainService domainService;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    @Tested
    ConsoleUserSecurityPolicyManager userSecurityPolicyManager;

    @Test
    public void testGetPasswordComplexityPatternProperty() {
        String result = userSecurityPolicyManager.getPasswordComplexityPatternProperty();
        Assert.assertEquals(ConsoleUserSecurityPolicyManager.PASSWORD_COMPLEXITY_PATTERN, result);
    }

    @Test
    public void testGetPasswordHistoryPolicyProperty() {
        String result = userSecurityPolicyManager.getPasswordHistoryPolicyProperty();
        Assert.assertEquals(ConsoleUserSecurityPolicyManager.PASSWORD_HISTORY_POLICY, result);
    }

    @Test
    public void testGetMaximumDefaultPasswordAgeProperty() {
        String result = userSecurityPolicyManager.getMaximumDefaultPasswordAgeProperty();
        Assert.assertEquals(ConsoleUserSecurityPolicyManager.MAXIMUM_DEFAULT_PASSWORD_AGE, result);
    }

    @Test
    public void testGetMaximumPasswordAgeProperty() {
        String result = userSecurityPolicyManager.getMaximumPasswordAgeProperty();
        Assert.assertEquals(ConsoleUserSecurityPolicyManager.MAXIMUM_PASSWORD_AGE, result);
    }

    @Test
    public void testGetWarningDaysBeforeExpiration() {
        String result = userSecurityPolicyManager.getWarningDaysBeforeExpiration();
        Assert.assertEquals(ConsoleUserSecurityPolicyManager.WARNING_DAYS_BEFORE_EXPIRATION, result);
    }

    @Test
    public void getMaxAttemptAmountTest() {
        User user = new User();
        user.setUserName("user1");
        user.addRole(new UserRole(AuthRole.ROLE_AP_ADMIN.name()));
        new Expectations() {{
            domainService.getDomain("default");
            result = DomainService.DEFAULT_DOMAIN;
            domibusPropertyProvider.getIntegerDomainProperty(DomainService.DEFAULT_DOMAIN, MAXIMUM_LOGIN_ATTEMPT);
            result = 20;
        }};

        int result = userSecurityPolicyManager.getMaxAttemptAmount(user);

        Assert.assertEquals(20, result);
    }

    @Test
    public void getCurrentOrDefaultDomainForUserTest() {

        new Expectations() {{
            domainContextProvider.getCurrentDomainSafely();
            result = DomainService.DEFAULT_DOMAIN;
            domibusPropertyProvider.getIntegerDomainProperty(LOGIN_SUSPENSION_TIME);
            result = 3600;
        }};

        int result = userSecurityPolicyManager.getSuspensionInterval();

        Assert.assertEquals(3600, result);
    }
}
