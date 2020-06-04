package eu.domibus.core.alerts.service;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.user.UserBase;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.configuration.model.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.configuration.model.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.configuration.model.LoginFailureModuleConfiguration;
import eu.domibus.core.alerts.configuration.model.RepetitiveAlertModuleConfiguration;
import eu.domibus.core.user.UserDaoBase;
import eu.domibus.core.user.UserEntityBase;
import eu.domibus.core.user.UserLoginErrorReason;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@Service
public abstract class UserAlertsServiceImpl implements UserAlertsService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserAlertsServiceImpl.class);
    private static final String DEFAULT = "default ";

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private EventService eventService;

    @Autowired
    private MultiDomainAlertConfigurationService alertsConfiguration;


    protected abstract String getMaximumDefaultPasswordAgeProperty();

    protected abstract String getMaximumPasswordAgeProperty();

    protected abstract AlertType getAlertTypeForPasswordImminentExpiration();

    protected abstract AlertType getAlertTypeForPasswordExpired();

    protected abstract EventType getEventTypeForPasswordImminentExpiration();

    protected abstract EventType getEventTypeForPasswordExpired();

    protected abstract UserDaoBase getUserDao();

    protected abstract UserEntityBase.Type getUserType();

    protected abstract AccountDisabledModuleConfiguration getAccountDisabledConfiguration();

    protected abstract AlertModuleConfigurationBase getAccountEnabledConfiguration();

    protected abstract LoginFailureModuleConfiguration getLoginFailureConfiguration();

    @Override
    public void triggerLoginEvents(String userName, UserLoginErrorReason userLoginErrorReason) {
        final LoginFailureModuleConfiguration loginFailureConfiguration = getLoginFailureConfiguration();
        LOG.debug("loginFailureConfiguration.isActive : [{}]", loginFailureConfiguration.isActive());
        switch (userLoginErrorReason) {
            case BAD_CREDENTIALS:
                if (loginFailureConfiguration.isActive()) {
                    eventService.enqueueLoginFailureEvent(getUserType(), userName, new Date(), false);
                }
                break;
            case INACTIVE:
            case SUSPENDED:
                final AccountDisabledModuleConfiguration accountDisabledConfiguration = getAccountDisabledConfiguration();
                if (accountDisabledConfiguration.isActive()) {
                    if (accountDisabledConfiguration.shouldTriggerAccountDisabledAtEachLogin()) {
                        eventService.enqueueAccountDisabledEvent(getUserType(), userName, new Date());
                    } else if (loginFailureConfiguration.isActive()) {
                        eventService.enqueueLoginFailureEvent(getUserType(), userName, new Date(), true);
                    }
                }
                break;
            case UNKNOWN:
                break;
        }
    }

    @Override
    public void triggerDisabledEvent(UserBase user) {
        final AccountDisabledModuleConfiguration accountDisabledConfiguration = getAccountDisabledConfiguration();
        if (accountDisabledConfiguration.isActive()) {
            LOG.debug("Sending account disabled event for user:[{}]", user.getUserName());
            eventService.enqueueAccountDisabledEvent(getUserType(), user.getUserName(), new Date());
        }
    }

    @Override
    public void triggerEnabledEvent(UserBase user) {
        final AlertModuleConfigurationBase accountEnabledConfiguration = getAccountEnabledConfiguration();
        if (accountEnabledConfiguration.isActive()) {
            LOG.debug("Sending account enabled event for user:[{}]", user.getUserName());
            eventService.enqueueAccountEnabledEvent(getUserType(), user.getUserName(), new Date());
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void triggerPasswordExpirationEvents() {
        try {
            triggerExpiredEvents(true);
            triggerExpiredEvents(false);
        } catch (Exception ex) {
            LOG.error("Send password expired alerts failed ", ex);
        }
        try {
            triggerImminentExpirationEvents(true);
            triggerImminentExpirationEvents(false);
        } catch (Exception ex) {
            LOG.error("Send imminent expiration alerts failed ", ex);
        }
    }

    protected void triggerImminentExpirationEvents(boolean usersWithDefaultPassword) {
        triggerExpirationEvents(usersWithDefaultPassword, getAlertTypeForPasswordImminentExpiration(),
                getEventTypeForPasswordImminentExpiration());
    }

    protected void triggerExpiredEvents(boolean usersWithDefaultPassword) {
        triggerExpirationEvents(usersWithDefaultPassword, getAlertTypeForPasswordExpired(),
                getEventTypeForPasswordExpired());
    }

    private void triggerExpirationEvents(boolean usersWithDefaultPassword, AlertType alertType, EventType eventType) {
        final RepetitiveAlertModuleConfiguration eventConfiguration = alertsConfiguration.getRepetitiveAlertConfiguration(alertType);
        if (!eventConfiguration.isActive()) {
            return;
        }
        final Integer duration = eventConfiguration.getEventDelay();
        String expirationProperty = usersWithDefaultPassword ? getMaximumDefaultPasswordAgeProperty() : getMaximumPasswordAgeProperty();
        int maxPasswordAgeInDays = domibusPropertyProvider.getIntegerProperty(expirationProperty);
        if (maxPasswordAgeInDays == 0) {
            // if password expiration is disabled, do not trigger the corresponding alerts, regardless of alert enabled/disabled status
            return;
        }

        LocalDate from;
        boolean imminent = (alertType == AlertType.PASSWORD_IMMINENT_EXPIRATION)
                || (alertType == AlertType.PLUGIN_PASSWORD_IMMINENT_EXPIRATION);
        if (imminent) {
            from = LocalDate.now().minusDays(maxPasswordAgeInDays);
        } else {
            from = LocalDate.now().minusDays(maxPasswordAgeInDays).minusDays(duration);
        }
        LocalDate to = from.plusDays(duration);
        LOG.debug("[{}]: Searching for {} users with password change date between [{}]->[{}]", alertType, (usersWithDefaultPassword ? DEFAULT : StringUtils.EMPTY), from, to);

        List<UserEntityBase> eligibleUsers = getUserDao().findWithPasswordChangedBetween(from, to, usersWithDefaultPassword);
        LOG.debug("[{}]: Found [{}] eligible {} users", alertType, (usersWithDefaultPassword ? DEFAULT : StringUtils.EMPTY), eligibleUsers.size());

        eligibleUsers.forEach(user -> {
            eventService.enqueuePasswordExpirationEvent(eventType, user, maxPasswordAgeInDays);
        });
    }
}
