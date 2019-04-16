package eu.domibus.security;

import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserEntityBase;
import eu.domibus.core.alerts.service.PluginUserAlertsServiceImpl;
import eu.domibus.core.security.AuthenticationDAO;
import eu.domibus.core.security.PluginUserPasswordHistoryDao;
import eu.domibus.security.PluginUserSecurityPolicyManager;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static eu.domibus.security.PluginUserSecurityPolicyManager.LOGIN_SUSPENSION_TIME;
import static eu.domibus.security.PluginUserSecurityPolicyManager.MAXIMUM_LOGIN_ATTEMPT;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

public class PluginUserSecurityPolicyManagerTest {

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    PluginUserPasswordHistoryDao userPasswordHistoryDao;

    @Injectable
    AuthenticationDAO userDao;

    @Injectable
    BCryptPasswordEncoder bcryptEncoder;

    @Injectable
    PluginUserAlertsServiceImpl userAlertsService;

    @Injectable
    UserDomainService userDomainService;

    @Injectable
    DomainService domainService;

    @Tested
    PluginUserSecurityPolicyManager userSecurityPolicyManager;

    @Test
    public void testGetPasswordComplexityPatternProperty() {
        String result = userSecurityPolicyManager.getPasswordComplexityPatternProperty();
        Assert.assertEquals(PluginUserSecurityPolicyManager.PASSWORD_COMPLEXITY_PATTERN, result);
    }

    @Test
    public void testGetPasswordHistoryPolicyProperty() {
        String result = userSecurityPolicyManager.getPasswordHistoryPolicyProperty();
        Assert.assertEquals(PluginUserSecurityPolicyManager.PASSWORD_HISTORY_POLICY, result);
    }

    @Test
    public void testGetMaximumDefaultPasswordAgeProperty() {
        String result = userSecurityPolicyManager.getMaximumDefaultPasswordAgeProperty();
        Assert.assertEquals(PluginUserSecurityPolicyManager.MAXIMUM_DEFAULT_PASSWORD_AGE, result);
    }

    @Test
    public void testGetMaximumPasswordAgeProperty() {
        String result = userSecurityPolicyManager.getMaximumPasswordAgeProperty();
        Assert.assertEquals(PluginUserSecurityPolicyManager.MAXIMUM_PASSWORD_AGE, result);
    }

    @Test
    public void testGetWarningDaysBeforeExpiration() {
        String result = userSecurityPolicyManager.getWarningDaysBeforeExpiration();
        Assert.assertEquals(PluginUserSecurityPolicyManager.WARNING_DAYS_BEFORE_EXPIRATION, result);
    }

    @Test
    public void testGetMaxAttemptAmount() {
        UserEntityBase user = new User();
        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(MAXIMUM_LOGIN_ATTEMPT);
            result = 5;
        }};

        int result = userSecurityPolicyManager.getMaxAttemptAmount(user);

        Assert.assertEquals(5, result);
    }

    @Test
    public void testGetSuspensionInterval() {

        new Expectations() {{
            domibusPropertyProvider.getIntegerDomainProperty(LOGIN_SUSPENSION_TIME);
            result = 3600;
        }};

        int result = userSecurityPolicyManager.getSuspensionInterval();

        Assert.assertEquals(3600, result);
    }
}
