package eu.domibus.plugin.fs.property;

import eu.domibus.common.NotificationType;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomibusPropertyExtService;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
public class FSPluginDomibusPropertyExtServiceMock implements DomibusPropertyExtService {

    @Override
    public String getProperty(String propertyName) {
        return null;
    }

    @Override
    public String getProperty(DomainDTO domain, String propertyName) {
        return null;
    }

    @Override
    public Integer getIntegerProperty(String propertyName) {
        return null;
    }

    @Override
    public Boolean getBooleanProperty(String propertyName) {
        return null;
    }

    @Override
    public Set<String> filterPropertiesName(Predicate<String> predicate) {
        return null;
    }

    @Override
    public List<String> getNestedProperties(String prefix) {
        return null;
    }

    @Override
    public String getDomainProperty(DomainDTO domain, String propertyName) {
        return null;
    }

    @Override
    public void setDomainProperty(DomainDTO domain, String propertyName, String propertyValue) {

    }

    @Override
    public List<NotificationType> getConfiguredNotifications(String notificationPropertyName) {
        return null;
    }

    @Override
    public void setProperty(String propertyName, String propertyValue) {

    }

    @Override
    public boolean containsDomainPropertyKey(DomainDTO domain, String propertyName) {
        return false;
    }

    @Override
    public boolean containsPropertyKey(String propertyName) {
        return false;
    }

    @Override
    public String getDomainProperty(DomainDTO domain, String propertyName, String defaultValue) {
        return null;
    }

    @Override
    public String getDomainResolvedProperty(DomainDTO domain, String propertyName) {
        return null;
    }

    @Override
    public String getResolvedProperty(String propertyName) {
        return null;
    }

    @Override
    public void setProperty(DomainDTO domain, String propertyName, String propertyValue, boolean broadcast) {

    }
}
