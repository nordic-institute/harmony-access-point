package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * This class has a similar behavior than @ConfigurationProperties annotation and allows
 * to parse list of properties in the format domibus.example.name[0],domibus.example.name[1].
 * <p>
 * Subclasses implement a transform method to create the model needed <E> and the map method will then return a List<E>.
 */

public abstract class PropertyGroupMapper<E> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PropertyGroupMapper.class);

    private final DomibusPropertyExtService domibusPropertyExtService;

    private final DomainContextExtService domainContextExtService;

    private final Environment environment;

    private final Pattern passwordPattern = Pattern.compile(".*password.*", Pattern.CASE_INSENSITIVE);


    public PropertyGroupMapper(final DomibusPropertyExtService domibusPropertyExtService,
                               final DomainContextExtService domainContextExtService,
                               final Environment environment) {
        this.domibusPropertyExtService = domibusPropertyExtService;
        this.domainContextExtService = domainContextExtService;
        this.environment = environment;
    }

    protected List<E> map(String... propertyNames) {
        boolean propertyEmpty = false;
        List<E> elements = new ArrayList<>();
        for (int count = 0; count < 100; count++) {
            Map<String, ImmutablePair<String, String>> keyValues = new HashMap<>();
            for (String propertyName : propertyNames) {
                final String format = propertyName + "[%s]";
                final String propertyKey = String.format(format, count);
                if (!propertyKeyExists(propertyKey)) {
                    propertyEmpty = true;
                    break;
                }
                final String propertyValue = getPropertyValue(propertyKey);
                if (!passwordPattern.matcher(propertyKey).matches()) {
                    LOG.debug("Property:[{}] has following value:[{}]", propertyKey, propertyValue);
                }
                keyValues.put(propertyName, new ImmutablePair<>(propertyName, propertyValue));
            }
            if (!propertyEmpty) {
                final E transform = transform(keyValues);
                if (transform != null) {
                    elements.add(transform);
                }
            } else {
                break;
            }
        }
        return elements;
    }

    private boolean propertyKeyExists(final String key) {
        boolean keyExist;
        DomainDTO currentDomain = domainContextExtService.getCurrentDomainSafely();
        if (currentDomain != null) {
            keyExist = domibusPropertyExtService.containsDomainPropertyKey(currentDomain, key);
            LOG.trace("Checking if key:[{}] exists in domain:[{}]:[{}]", key, currentDomain, keyExist);
        } else {
            keyExist = domibusPropertyExtService.containsPropertyKey(key);
            LOG.trace("Checking if key:[{}] exists in default domain configuration:[{}]", key, keyExist);
        }
        if (!keyExist) {
            keyExist = environment.containsProperty(key);
            LOG.trace("Checking if key:[{}] exists in spring environment:[{}]", key, keyExist);
        }
        return keyExist;
    }

    private String getPropertyValue(String key) {
        String propertyValue = null;
        DomainDTO currentDomain = domainContextExtService.getCurrentDomainSafely();
        if (currentDomain != null) {
            propertyValue = domibusPropertyExtService.getDomainProperty(currentDomain, key);
        } else {
            propertyValue = domibusPropertyExtService.getProperty(key);
        }
        if (StringUtils.isEmpty(propertyValue)) {
            propertyValue = environment.getProperty(key);
        }
        LOG.trace("Property with key:[{}] has value:[{}]", key, propertyValue);
        return propertyValue;
    }

    abstract E transform(Map<String, ImmutablePair<String, String>> keyValues);

}
