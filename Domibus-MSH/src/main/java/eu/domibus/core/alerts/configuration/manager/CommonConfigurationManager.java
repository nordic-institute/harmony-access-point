package eu.domibus.core.alerts.configuration.manager;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.model.CommonConfiguration;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

@Service
public class CommonConfigurationManager {
    private static final Logger LOG = DomibusLoggerFactory.getLogger(CommonConfigurationManager.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    private ConfigurationLoader<CommonConfiguration> loader;

    public CommonConfiguration getConfiguration() {
        return loader.getConfiguration(this::readConfiguration);
    }

    public void reset() {
        loader.resetConfiguration();
    }

    protected CommonConfiguration readConfiguration() {
        final boolean emailActive = domibusPropertyProvider.getBooleanProperty(getSendEmailActivePropertyName());
        final Integer alertLifeTimeInDays = domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_CLEANER_ALERT_LIFETIME);

        if (!emailActive) {
            return new CommonConfiguration(alertLifeTimeInDays);
        }

        return readDomainEmailConfiguration(alertLifeTimeInDays);
    }

    protected CommonConfiguration readDomainEmailConfiguration(Integer alertLifeTimeInDays) {
        final String alertEmailSender = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SENDER_EMAIL);
        final String alertEmailReceiver = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_RECEIVER_EMAIL);

        boolean misConfigured = false;
        if (StringUtils.isEmpty(alertEmailReceiver) || StringUtils.isEmpty(alertEmailSender)) {
            misConfigured = true;
        } else {
            List<String> emailsToValidate = new ArrayList<>(Arrays.asList(alertEmailSender));
            emailsToValidate.addAll(Arrays.asList(alertEmailReceiver.split(";")));
            for (String email : emailsToValidate) {
                misConfigured = !isValidEmail(email);
                if (misConfigured) {
                    break;
                }
            }
        }
        if (misConfigured) {
            Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
            LOG.error("Alert module can not send email, mail sender property name:[{}]/value[{}] and receiver property name:[{}]/value[{}] are mandatory in the domain [{}].",
                    DOMIBUS_ALERT_SENDER_EMAIL, alertEmailSender, DOMIBUS_ALERT_RECEIVER_EMAIL, alertEmailReceiver, currentDomain);
            throw new IllegalArgumentException("Invalid email address configured for the alert module.");
        }
        return new CommonConfiguration(alertLifeTimeInDays, alertEmailSender, alertEmailReceiver);
    }

    protected boolean isValidEmail(String email) {
        try {
            InternetAddress address = new InternetAddress(email);
            address.validate();
            return true;
        } catch (AddressException ae) {
            LOG.trace("Email address [{}] is not valid:", email, ae);
            return false;
        }
    }

    private String getSendEmailActivePropertyName() {
        return DOMIBUS_ALERT_MAIL_SENDING_ACTIVE;
    }
}
