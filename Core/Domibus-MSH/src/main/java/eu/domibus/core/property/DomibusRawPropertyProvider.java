package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyException;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public interface DomibusRawPropertyProvider {

    /**
     * Retrieves the property value, taking into account the property usages and the current domain.
     * If needed, it falls back to the default value provided in the global properties set.
     */
    String getRawPropertyValue(String propertyName) throws DomibusPropertyException;

    /**
     * Retrieves the property value from the requested domain. If not found, fall back to the property value from the global properties set.
     *
     * @param domain       the domain.
     * @param propertyName the property name.
     * @return the value for that property.
     */
    String getRawPropertyValue(Domain domain, String propertyName) throws DomibusPropertyException;
}
