package eu.domibus.spring.properties;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import java.util.Properties;
import java.util.Set;

public class PrefixedProperties extends Properties {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PrefixedProperties.class);

    public PrefixedProperties(DomibusPropertyProvider domibusPropertyProvider, String prefix) {
        Set<String> propertyNames = domibusPropertyProvider.filterPropertiesName(propName -> propName.startsWith(prefix));
        LOG.debug("The following property names found with prefix [{}]: [{}]", prefix, propertyNames);

        for (String propertyName : propertyNames) {
            String key = propertyName.substring(prefix.length());
            String resolved = domibusPropertyProvider.getProperty(propertyName);
            LOG.trace("Adding property [{}]", key);
            this.setProperty(key, (String) resolved);
        }
    }
}