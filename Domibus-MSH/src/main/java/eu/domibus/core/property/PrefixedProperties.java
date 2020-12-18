package eu.domibus.core.property;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.Properties;
import java.util.Set;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
public class PrefixedProperties extends Properties {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PrefixedProperties.class);

    public PrefixedProperties(DomibusPropertyProvider domibusPropertyProvider, String prefix) {
        Set<String> propertyNames = domibusPropertyProvider.filterPropertiesName(propName -> propName.startsWith(prefix));
        LOG.debug("The following property names found with prefix [{}]: [{}]", prefix, propertyNames);

        for (String propertyName : propertyNames) {
            String key = propertyName.substring(prefix.length());
            if (StringUtils.isEmpty(key)) {
                LOG.warn("Empty key detected for prefix [{}]", prefix);
                continue;
            }
            String resolved = domibusPropertyProvider.getProperty(propertyName);
            LOG.trace("Adding property [{}]", key);
            this.setProperty(key, resolved);
        }
    }
}