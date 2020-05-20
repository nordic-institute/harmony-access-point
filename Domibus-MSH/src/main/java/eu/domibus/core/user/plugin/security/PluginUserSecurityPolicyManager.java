package eu.domibus.core.user.plugin.security;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.user.UserDaoBase;
import eu.domibus.core.user.UserPasswordHistoryDao;
import eu.domibus.core.user.UserEntityBase;
import eu.domibus.core.alerts.service.PluginUserAlertsServiceImpl;
import eu.domibus.core.alerts.service.UserAlertsService;
import eu.domibus.core.user.plugin.AuthenticationDAO;
import eu.domibus.core.user.plugin.AuthenticationEntity;
import eu.domibus.core.user.plugin.security.password.PluginUserPasswordHistoryDao;
import eu.domibus.core.user.UserSecurityPolicyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

@Service
public class PluginUserSecurityPolicyManager extends UserSecurityPolicyManager<AuthenticationEntity> {

    static final String PASSWORD_COMPLEXITY_PATTERN = DOMIBUS_PLUGIN_PASSWORD_POLICY_PATTERN; //NOSONAR
    static final String PASSWORD_HISTORY_POLICY = DOMIBUS_PASSWORD_POLICY_PLUGIN_DONT_REUSE_LAST; //NOSONAR

    final static String MAXIMUM_PASSWORD_AGE = DOMIBUS_PASSWORD_POLICY_PLUGIN_EXPIRATION; //NOSONAR
    final static String MAXIMUM_DEFAULT_PASSWORD_AGE = DOMIBUS_PASSWORD_POLICY_PLUGIN_DEFAULT_PASSWORD_EXPIRATION; //NOSONAR

    protected static final String MAXIMUM_LOGIN_ATTEMPT = DOMIBUS_PLUGIN_LOGIN_MAXIMUM_ATTEMPT;

    protected static final String LOGIN_SUSPENSION_TIME = DOMIBUS_PLUGIN_LOGIN_SUSPENSION_TIME;

    @Autowired
    protected AuthenticationDAO userDao;

    @Autowired
    private PluginUserPasswordHistoryDao userPasswordHistoryDao;

    @Autowired
    private PluginUserAlertsServiceImpl userAlertsService;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;


    @Override
    protected String getPasswordComplexityPatternProperty() {
        return PASSWORD_COMPLEXITY_PATTERN;
    }

    @Override
    public String getPasswordHistoryPolicyProperty() {
        return PASSWORD_HISTORY_POLICY;
    }

    @Override
    protected String getMaximumDefaultPasswordAgeProperty() {
        return MAXIMUM_DEFAULT_PASSWORD_AGE;
    }

    @Override
    protected String getMaximumPasswordAgeProperty() {
        return MAXIMUM_PASSWORD_AGE;
    }

    @Override
    protected String getWarningDaysBeforeExpirationProperty() {
        return null;
    }

    @Override
    protected UserPasswordHistoryDao getUserHistoryDao() {
        return userPasswordHistoryDao;
    }

    @Override
    protected UserDaoBase getUserDao() {
        return userDao;
    }

    @Override
    protected int getMaxAttemptAmount(UserEntityBase user) {
        return domibusPropertyProvider.getIntegerProperty(MAXIMUM_LOGIN_ATTEMPT);
    }

    @Override
    protected UserAlertsService getUserAlertsService() {
        return userAlertsService;
    }

    @Override
    protected int getSuspensionInterval() {
        return domibusPropertyProvider.getIntegerProperty(LOGIN_SUSPENSION_TIME);
    }

    @Override
    protected UserEntityBase.Type getUserType() { return UserEntityBase.Type.PLUGIN; }
}
