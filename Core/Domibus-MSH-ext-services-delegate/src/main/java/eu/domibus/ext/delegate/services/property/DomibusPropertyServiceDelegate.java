package eu.domibus.ext.delegate.services.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.NotificationType;
import eu.domibus.ext.delegate.mapper.DomibusExtMapper;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class DomibusPropertyServiceDelegate implements DomibusPropertyExtService {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusPropertyServiceDelegate.class);

    protected DomibusPropertyProvider domibusPropertyProvider;
    protected DomainService domainService;

    protected DomibusExtMapper domibusExtMapper;

    final DomainContextExtService domainContextService;

    public DomibusPropertyServiceDelegate(DomibusPropertyProvider domibusPropertyProvider,
                                          DomainService domainService,
                                          DomibusExtMapper domibusExtMapper,
                                          DomainContextExtService domainContextService) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domainService = domainService;
        this.domibusExtMapper = domibusExtMapper;
        this.domainContextService = domainContextService;
    }

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
    public List<NotificationType> getConfiguredNotifications(String notificationPropertyName) {
        String messageNotificationPropertyValue = getProperty(notificationPropertyName);
        if (StringUtils.isEmpty(messageNotificationPropertyValue)) {
            LOG.debug("Property [{}] value is empty", notificationPropertyName);
            return Collections.EMPTY_LIST;
        }
        LOG.debug("Property [{}] value is [{}]", notificationPropertyName, messageNotificationPropertyValue);
        String[] messageNotifications = StringUtils.split(messageNotificationPropertyValue, ",");
        return Arrays.stream(messageNotifications)
                .map(this::getNotificationType)
                .filter(Objects::nonNull)
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
            LOG.warn("Unrecognized notification type [{}]", trimmedValue);
            LOG.trace("Invalid notification type [{}] throws exception", trimmedValue, e);
            return null;
        }
    }

    protected String getDomainProperty(DomainDTO domain, String propertyName) {
        final Domain domibusDomain = domibusExtMapper.domainDTOToDomain(domain);
        return domibusPropertyProvider.getProperty(domibusDomain, propertyName);
    }

    @Override
    public void setProperty(String propertyName, String propertyValue) {
        DomainDTO currentDomain = domainContextService.getCurrentDomainSafely();
        Domain domibusDomain = domibusExtMapper.domainDTOToDomain(currentDomain);

        domibusPropertyProvider.setProperty(domibusDomain, propertyName, propertyValue);
    }

    @Override
    public boolean containsDomainPropertyKey(DomainDTO domainDTO, String propertyName) {
        final Domain domain = domibusExtMapper.domainDTOToDomain(domainDTO);
        return domibusPropertyProvider.containsDomainPropertyKey(domain, propertyName);
    }

    @Override
    public boolean containsPropertyKey(String propertyName) {
        return domibusPropertyProvider.containsPropertyKey(propertyName);
    }

    @Override
    public void setProperty(DomainDTO domain, String propertyName, String propertyValue, boolean broadcast) {
        final Domain domibusDomain = domibusExtMapper.domainDTOToDomain(domain);
        domibusPropertyProvider.setProperty(domibusDomain, propertyName, propertyValue, broadcast);
    }

    @Override
    public void loadProperties(DomainDTO domain, String propertiesFilePath) {
        final Domain domibusDomain = domibusExtMapper.domainDTOToDomain(domain);
        domibusPropertyProvider.loadProperties(domibusDomain, propertiesFilePath);
    }

    @Override
    public void removeProperties(DomainDTO domain, String propertiesFilePath) {
        final Domain domibusDomain = domibusExtMapper.domainDTOToDomain(domain);
        domibusPropertyProvider.removeProperties(domibusDomain, propertiesFilePath);
    }
}
