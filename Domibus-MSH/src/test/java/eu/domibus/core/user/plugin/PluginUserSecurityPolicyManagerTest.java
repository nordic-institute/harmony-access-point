package eu.domibus.core.user.plugin;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.multitenancy.UserSessionsService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.user.User;
import eu.domibus.core.user.UserEntityBase;
import eu.domibus.core.alerts.service.PluginUserAlertsServiceImpl;
import eu.domibus.core.user.plugin.AuthenticationDAO;
import eu.domibus.core.user.plugin.PluginUserPasswordHistoryDao;
import eu.domibus.core.user.plugin.PluginUserSecurityPolicyManager;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static eu.domibus.core.user.plugin.PluginUserSecurityPolicyManager.LOGIN_SUSPENSION_TIME;
import static eu.domibus.core.user.plugin.PluginUserSecurityPolicyManager.MAXIMUM_LOGIN_ATTEMPT;

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

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    @Injectable
    UserSessionsService userSessionsService;

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
        String result = userSecurityPolicyManager.getWarningDaysBeforeExpirationProperty();
        Assert.assertEquals(null, result);
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
            domibusPropertyProvider.getIntegerProperty(LOGIN_SUSPENSION_TIME);
            result = 3600;
        }};

        int result = userSecurityPolicyManager.getSuspensionInterval();

        Assert.assertEquals(3600, result);
    }
}
