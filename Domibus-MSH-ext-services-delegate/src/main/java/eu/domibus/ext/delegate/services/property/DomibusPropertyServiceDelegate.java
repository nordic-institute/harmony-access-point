package eu.domibus.ext.delegate.services.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.NotificationType;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class DomibusPropertyServiceDelegate implements DomibusPropertyExtService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusPropertyServiceDelegate.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomainExtConverter domainConverter;

    @Autowired
    DomainContextExtService domainContextService;

    @Override
    public String getProperty(String propertyName) {
        return domibusPropertyProvider.getProperty(propertyName);
    }

    @Override
    public String getProperty(DomainDTO domain, String propertyName) {
        return getDomainProperty(domain, propertyName);
    }

    @Override
    public Integer getIntegerProperty(String propertyName) {
        return domibusPropertyProvider.getIntegerProperty((propertyName));
    }

    @Override
    public Boolean getBooleanProperty(String propertyName) {
        return domibusPropertyProvider.getBooleanProperty(propertyName);
    }

    @Override
    public Set<String> filterPropertiesName(Predicate<String> predicate) {
        return domibusPropertyProvider.filterPropertiesName(predicate);
    }

    @Override
    public List<String> getNestedProperties(String prefix) {
        return domibusPropertyProvider.getNestedProperties(prefix);
    }

    @Override
    public List<String> getAllNestedProperties(String prefix) {
        return domibusPropertyProvider.getAllNestedProperties(prefix);
    }

    @Override
    public List<NotificationType> getConfiguredNotifications(String notificationPropertyName) {
        String messageNotificationPropertyValue = getProperty(notificationPropertyName);
        if (StringUtils.isEmpty(messageNotificationPropertyValue)) {
            LOG.debug("Property [{}] value is empty", notificationPropertyName);
            return Collections.EMPTY_LIST;
        }
        LOG.debug("Property [{}] value is [{}]", notificationPropertyName, messageNotificationPropertyValue);
        String[] messageNotifications = StringUtils.split(messageNotificationPropertyValue, ",");
        return Arrays.asList(messageNotifications).stream()
                .map(notificationValue -> getNotificationType(notificationValue))
                .filter(notificationType -> notificationType != null)
                .distinct()
                .collect(Collectors.toList());
    }

    protected NotificationType getNotificationType(String notificationValue) {
        String trimmedValue = StringUtils.trim(notificationValue);
        if (StringUtils.isBlank(trimmedValue)) {
            LOG.warn("Empty notification type ignored");
            return null;
        }

        try {
            return NotificationType.valueOf(trimmedValue);
        } catch (IllegalArgumentException e) {
            LOG.warn("Unrecognized notification type [{}]", trimmedValue, e);
            return null;
        }
    }

    @Override
    public String getDomainProperty(DomainDTO domain, String propertyName) {
        final Domain domibusDomain = domainConverter.convert(domain, Domain.class);
        return domibusPropertyProvider.getProperty(domibusDomain, propertyName);
    }

    @Override
    public void setDomainProperty(DomainDTO domain, String propertyName, String propertyValue) {
        final Domain domibusDomain = domainConverter.convert(domain, Domain.class);
        domibusPropertyProvider.setProperty(domibusDomain, propertyName, propertyValue);
    }

    @Override
    public void setProperty(String propertyName, String propertyValue) {
        DomainDTO currentDomain = domainContextService.getCurrentDomainSafely();
        Domain domibusDomain = domainConverter.convert(currentDomain, Domain.class);

        domibusPropertyProvider.setProperty(domibusDomain, propertyName, propertyValue);
    }

    @Override
    public boolean containsDomainPropertyKey(DomainDTO domainDTO, String propertyName) {
        final Domain domain = domainConverter.convert(domainDTO, Domain.class);
        return domibusPropertyProvider.containsDomainPropertyKey(domain, propertyName);
    }

    @Override
    public boolean containsPropertyKey(String propertyName) {
        return domibusPropertyProvider.containsPropertyKey(propertyName);
    }

    @Override
    public String getDomainProperty(DomainDTO domainCode, String propertyName, String defaultValue) {
        final Domain domain = domainConverter.convert(domainCode, Domain.class);
        String value = domibusPropertyProvider.getProperty(domain, propertyName);
        if (StringUtils.isEmpty(value)) {
            value = defaultValue;
        }
        return value;
    }

    @Override
    public String getDomainResolvedProperty(DomainDTO domainCode, String propertyName) {
        return getDomainProperty(domainCode, propertyName);
    }

    @Override
    public String getResolvedProperty(String propertyName) {
        return getProperty(propertyName);
    }

    @Override
    public void setProperty(DomainDTO domain, String propertyName, String propertyValue, boolean broadcast) {
        final Domain domibusDomain = domainConverter.convert(domain, Domain.class);
        domibusPropertyProvider.setProperty(domibusDomain, propertyName, propertyValue, broadcast);
    }
}
