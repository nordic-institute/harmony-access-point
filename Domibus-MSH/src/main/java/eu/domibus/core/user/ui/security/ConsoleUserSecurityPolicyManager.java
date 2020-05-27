package eu.domibus.core.user.ui.security;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.service.ConsoleUserAlertsServiceImpl;
import eu.domibus.core.alerts.service.UserAlertsService;
import eu.domibus.core.user.*;
import eu.domibus.core.user.UserSecurityPolicyManager;
import eu.domibus.core.user.ui.User;
import eu.domibus.core.user.ui.UserDao;
import eu.domibus.core.user.ui.security.password.ConsoleUserPasswordHistoryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Ion Perpegel
 * @since 4.1
 * Template method pattern derived class responsible for particularities of console users
 */

@Service
public class ConsoleUserSecurityPolicyManager extends UserSecurityPolicyManager<User> {
    static final String WARNING_DAYS_BEFORE_EXPIRATION = DOMIBUS_PASSWORD_POLICY_WARNING_BEFORE_EXPIRATION;

    static final String PASSWORD_COMPLEXITY_PATTERN = DOMIBUS_PASSWORD_POLICY_PATTERN; //NOSONAR
    static final String PASSWORD_HISTORY_POLICY = DOMIBUS_PASSWORD_POLICY_DONT_REUSE_LAST; //NOSONAR

    static final String MAXIMUM_PASSWORD_AGE = DOMIBUS_PASSWORD_POLICY_EXPIRATION; //NOSONAR
    static final String MAXIMUM_DEFAULT_PASSWORD_AGE = DOMIBUS_PASSWORD_POLICY_DEFAULT_PASSWORD_EXPIRATION; //NOSONAR

    protected static final String MAXIMUM_LOGIN_ATTEMPT = DOMIBUS_CONSOLE_LOGIN_MAXIMUM_ATTEMPT;

    protected static final String LOGIN_SUSPENSION_TIME = DOMIBUS_CONSOLE_LOGIN_SUSPENSION_TIME;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected UserDao userDao;

    @Autowired
    private ConsoleUserPasswordHistoryDao userPasswordHistoryDao;

    @Autowired
    private ConsoleUserAlertsServiceImpl userAlertsService;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Override
    protected String getPasswordComplexityPatternProperty() {
        return PASSWORD_COMPLEXITY_PATTERN;
    }

    @Override
    public String getPasswordHistoryPolicyProperty() {
        return PASSWORD_HISTORY_POLICY;
    }

    @Override
    public String getMaximumDefaultPasswordAgeProperty() {
        return MAXIMUM_DEFAULT_PASSWORD_AGE;
    }

    @Override
    protected String getMaximumPasswordAgeProperty() {
        return MAXIMUM_PASSWORD_AGE;
    }

    @Override
    public String getWarningDaysBeforeExpirationProperty() {
        return WARNING_DAYS_BEFORE_EXPIRATION;
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
    protected UserEntityBase.Type getUserType() {
        return UserEntityBase.Type.CONSOLE;
    }

}
