package eu.domibus.core.property;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author Cosmin Baciu, Ion Perpegel
 * @since 4.0
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class DomibusPropertyProviderImpl implements DomibusPropertyProvider {

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Autowired
    DomibusPropertyMetadataManagerImpl domibusPropertyMetadataManager;

    @Autowired
    ApplicationContext applicationContext;

    /**
     * We inject here all managers: one for each plugin + domibus property manager delegate (which adapts DomibusPropertyManager to DomibusPropertyManagerExt)
     */
    //@Autowired
    //private List<DomibusPropertyManagerExt> propertyManagers;

    public String getProperty(String propertyName) {
        DomibusPropertyMetadata prop = getPropertyMetadata(propertyName);

        if (prop.isOnlyGlobal()) {                                          //prop is only global so the current domain doesn't matter
            return getGlobalProperty(prop);
        }

        if (!domibusConfigurationService.isMultiTenantAware()) {             //single-tenancy mode
            return getGlobalProperty(prop);
        }

        //multi-tenancy mode
        //domain or super property or a combination of 2 ( but not 3)
        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        if (currentDomain != null) {                                         //we have a domain in context so try a domain property
            if (prop.isDomain()) {
                return getDomainOrDefault(propertyName, prop, currentDomain);
            }
            LOGGER.error("Property [{}] is not applicable for a specific domain so null was returned.", propertyName);
            return null;
        } else {                                        //current domain being null, it is super or global property (but not both for now, although it could be and then the global takes precedence)
            if (prop.isGlobal()) {
                return getGlobalProperty(prop);
            }
            if (prop.isSuper()) {
                return getSuperOrDefault(propertyName, prop);
            }
            LOGGER.error("Property [{}] is not applicable for super users so null was returned.", propertyName);
            return null;
        }
    }

    public String getProperty(Domain domain, String propertyName) {
        DomibusPropertyMetadata prop = getPropertyMetadata(propertyName);

        //TODO: to be decided if we accept null as global or super domains( probably not a good idea)
        if (domain == null) {
            LOGGER.error("Domain cannot be null.");
            // return null;
            throw new IllegalArgumentException("Domain cannot be null for " + propertyName);
        }

        if (!domibusConfigurationService.isMultiTenantAware()) {             //single-tenancy mode
            return getGlobalProperty(prop);
        }

        if (!prop.isDomain()) {
            LOGGER.error("Property [{}] is not domain specific so it cannot be called with a domain [{}].", propertyName, domain);
            // return null;
            throw new IllegalArgumentException("Property " + propertyName + " is not domain specific so it cannot be retrieved for domain " + domain);
        }

        return getDomainOrDefault(propertyName, prop, domain);
    }

    private String getGlobalProperty(DomibusPropertyMetadata prop) {
        return getPropertyValue(prop.getName(), null, prop.isEncrypted());
    }

    private DomibusPropertyMetadata getPropertyMetadata(String propertyName) {

        DomibusPropertyMetadata prop = domibusPropertyMetadataManager.getKnownProperties().get(propertyName);

        // TODO review this
        if (prop == null) {
            Map<String, DomibusPropertyManagerExt> propertyManagers = applicationContext.getBeansOfType(DomibusPropertyManagerExt.class);

            for (DomibusPropertyManagerExt propertyManager : propertyManagers.values()) {
                DomibusPropertyMetadataDTO p = propertyManager.getKnownProperties().get(propertyName);
                if (p != null) {
                    LOGGER.warn("External property [{}] retrieved through DomibusPropertyProvider", propertyName);
                    prop = new DomibusPropertyMetadata(p.getName(), p.getModule(), p.isWritable(), p.getType(), p.isWithFallback(), p.isClusterAware(), p.isEncrypted());
                    break;
                }
            }
        }

        if (prop == null) {
            LOGGER.error("Property [{}] has no metadata defined.", propertyName);
            throw new IllegalArgumentException("Unknown property: " + propertyName);
        }
        return prop;
    }
    /*
    private DomibusPropertyMetadata getPropertyMetadata(String propertyName) {
        DomibusPropertyMetadata prop = domibusPropertyMetadataManager.getKnownProperties().get(propertyName);
        if (prop == null) {
            LOGGER.error("Property [{}] has no metadata defined.", propertyName);
            throw new IllegalArgumentException("Unknown property: " + propertyName);
        }
        return prop;
    }*/

    private String getDomainOrDefault(String propertyName, DomibusPropertyMetadata prop, Domain domain) {
        String specificPropertyName = getPropertyName(domain, propertyName);
        return getPropValueOrDefault(specificPropertyName, prop, domain, propertyName);
    }

    private String getSuperOrDefault(String propertyName, DomibusPropertyMetadata prop) {
        String specificPropertyName = "super." + propertyName;
        return getPropValueOrDefault(specificPropertyName, prop, null, propertyName);
    }

    private String getPropValueOrDefault(String specificPropertyName, DomibusPropertyMetadata prop, Domain domain, String originalPropertyName) {
        String propValue = getPropertyValue(specificPropertyName, domain, prop.isEncrypted());
        if (propValue != null) { // found a value->return it
            LOGGER.debug("Returned specific value for property [{}] on domain [{}].", originalPropertyName, domain);
            return propValue;
        }
        //didn't find a specific value
        //check if fallback is acceptable
        if (prop.isWithFallback()) {    //fall-back on default value from global file
            propValue = getPropertyValue(originalPropertyName, domain, prop.isEncrypted());
            if (propValue != null) { // found a value->return it
                LOGGER.debug("Returned fallback value for property [{}] on domain [{}].", originalPropertyName, domain);
                return propValue;
            }
        }
        LOGGER.warn("Could not find a value for property [{}] on domain [{}].", originalPropertyName, domain);
        return null;
    }

    ///////////////

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusPropertyProviderImpl.class);

    @Autowired
    @Qualifier("domibusProperties")
    protected Properties domibusProperties;

    @Autowired
    @Qualifier("domibusDefaultProperties")
    protected Properties domibusDefaultProperties;

    @Autowired
    protected PropertyResolver propertyResolver;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected PasswordEncryptionService passwordEncryptionService;

    protected String getPropertyName(Domain domain, String propertyName) {
        return domain.getCode() + "." + propertyName;
    }

//    @Override
//    public String getProperty(Domain domain, String propertyName) {
//        return getProperty(domain, propertyName, false);
//    }

//    @Override
//    private String getProperty(Domain domain, String propertyName, boolean decrypt) {
//        final String domainPropertyName = getPropertyName(domain, propertyName);
//        String propertyValue = getPropertyValue(domainPropertyName, domain, decrypt);
//        if (StringUtils.isEmpty(propertyValue) && DomainService.DEFAULT_DOMAIN.equals(domain)) {
//            propertyValue = getPropertyValue(propertyName, domain, decrypt);
//        }
//
//        return propertyValue;
//    }

    /**
     * Get the value from the system environment properties; if not found get the value from the system properties; if not found get the value from Domibus properties;
     * if still not found, look inside the Domibus default properties.
     *
     * @param propertyName the property name
     * @return The value of the property as found in the system properties, the Domibus properties or inside the default Domibus properties.
     */
    protected String getPropertyValue(String propertyName, Domain domain, boolean decrypt) {
        String result = System.getenv(propertyName);
        if (StringUtils.isEmpty(result)) {
            result = System.getProperty(propertyName);
        }
        if (StringUtils.isEmpty(result)) {
            result = domibusProperties.getProperty(propertyName);

            // There is no need to retrieve the default Domibus property value here since the Domibus properties above will contain it, unless overwritten by users.
            // For String property values, if users have overwritten their original default Domibus property values, it is their responsibility to ensure they are valid.
            // For all the other Boolean and Integer property values, if users have overwritten their original default Domibus property values, they are defaulted back to their
            // original default Domibus values when invalid (please check the #getInteger..(..) and #getBoolean..(..) methods below).
        }
        if (StringUtils.contains(result, "${")) {
            LOGGER.debug("Resolving property [{}]", propertyName);
            result = propertyResolver.getResolvedValue(result, domibusProperties, true);
        }
        if (decrypt && passwordEncryptionService.isValueEncrypted(result)) {
            LOGGER.debug("Decrypting property [{}]", propertyName);
            result = passwordEncryptionService.decryptProperty(domain, propertyName, result);
        }

        return result;
    }

//    @Override
//    public String getProperty(String propertyName) {
//        return getProperty(propertyName, false);
//    }

//    @Override
//    private String getProperty(String propertyName, boolean decrypt) {
//        return getPropertyValue(propertyName, null, decrypt);
//    }

    /**
     * Retrieves the property value from the requested domain.
     * If not found, fall back to the property value from the default domain.
     */
//    @Override
//    public String getProperty(Domain domain, String propertyName) {
//        String propertyValue = getProperty(domain, propertyName);
//        if (StringUtils.isEmpty(propertyValue) && !DomainService.DEFAULT_DOMAIN.equals(domain)) {
//            propertyValue = getProperty(DomainService.DEFAULT_DOMAIN, propertyName);
//        }
//        return propertyValue;
//    }
    @Override
    public Set<String> filterPropertiesName(Predicate<String> predicate) {
        Set<String> filteredPropertyNames = new HashSet<>();
        final Enumeration<?> enumeration = domibusProperties.propertyNames();
        while (enumeration.hasMoreElements()) {
            final String propertyName = (String) enumeration.nextElement();
            if (predicate.test(propertyName)) {
                filteredPropertyNames.add(propertyName);
            }
        }
        return filteredPropertyNames;
    }

    @Override
    public Integer getIntegerProperty(String propertyName) {
        String value = getProperty(propertyName);
        return getIntegerInternal(propertyName, value);
    }

//    @Override
//    public Integer getIntegerDomainProperty(String propertyName) {
//        String domainValue = getProperty(propertyName);
//        return getIntegerInternal(propertyName, domainValue);
//    }

//    @Override
//    public Integer getIntegerProperty(Domain domain, String propertyName) {
//        String domainValue = getProperty(domain, propertyName);
//        return getIntegerInternal(propertyName, domainValue);
//    }

    @Override
    public Long getLongProperty(String propertyName) {
        String value = getProperty(propertyName);
        return getLongInternal(propertyName, value);
    }

    private Integer getIntegerInternal(String propertyName, String customValue) {
        if (customValue != null) {
            try {
                return Integer.valueOf(customValue);
            } catch (final NumberFormatException e) {
                LOGGER.warn("Could not parse the property [" + propertyName + "] custom value [" + customValue + "] to an integer value", e);
                return getDefaultIntegerValue(propertyName);
            }
        }
        return getDefaultIntegerValue(propertyName);
    }

    protected Long getLongInternal(String propertyName, String customValue) {
        if (customValue != null) {
            try {
                return Long.valueOf(customValue);
            } catch (final NumberFormatException e) {
                LOGGER.warn("Could not parse the property [" + propertyName + "] custom value [" + customValue + "] to a Long value", e);
                return getDefaultLongValue(propertyName);
            }
        }
        return getDefaultLongValue(propertyName);
    }

    protected Integer getDefaultIntegerValue(String propertyName) {
        Integer defaultValue = MapUtils.getInteger(domibusDefaultProperties, propertyName);
        return checkDefaultValue(propertyName, defaultValue);
    }

    protected Long getDefaultLongValue(String propertyName) {
        Long defaultValue = MapUtils.getLong(domibusDefaultProperties, propertyName);
        return checkDefaultValue(propertyName, defaultValue);
    }

    @Override
    public Boolean getBooleanProperty(String propertyName) {
        String value = getProperty(propertyName);
        return getBooleanInternal(propertyName, value);
    }

//    @Override
//    public Boolean getBooleanDomainProperty(String propertyName) {
//        String domainValue = getProperty(propertyName);
//        return getBooleanInternal(propertyName, domainValue);
//    }

    @Override
    public Boolean getBooleanProperty(Domain domain, String propertyName) {
        String domainValue = getProperty(domain, propertyName);
        return getBooleanInternal(propertyName, domainValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsDomainPropertyKey(Domain domain, String propertyName) {
        final String domainPropertyName = getPropertyName(domain, propertyName);
        boolean domainPropertyKeyFound = domibusProperties.containsKey(domainPropertyName);
        if (!domainPropertyKeyFound) {
            domainPropertyKeyFound = domibusProperties.containsKey(propertyName);
        }
        return domainPropertyKeyFound;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsPropertyKey(String propertyName) {
        return domibusProperties.containsKey(propertyName);
    }

    private Boolean getBooleanInternal(String propertyName, String customValue) {
        if (customValue != null) {
            Boolean customBoolean = BooleanUtils.toBooleanObject(customValue);
            if (customBoolean != null) {
                return customBoolean;
            }
            LOGGER.warn("Could not parse the property [{}] custom value [{}] to a boolean value", propertyName, customValue);
            return getDefaultBooleanValue(propertyName);
        }
        return getDefaultBooleanValue(propertyName);
    }

    private Boolean getDefaultBooleanValue(String propertyName) {
        // We need to fetch the Boolean value in two steps as the MapUtils#getBoolean(Properties, String) does not return "null" when the value is an invalid Boolean.
        String defaultValue = MapUtils.getString(domibusDefaultProperties, propertyName);
        Boolean defaultBooleanValue = BooleanUtils.toBooleanObject(defaultValue);
        return checkDefaultValue(propertyName, defaultBooleanValue);
    }

    private <T> T checkDefaultValue(String propertyName, T defaultValue) {
        if (defaultValue == null) {
            throw new IllegalStateException("The default property [" + propertyName + "] is required but was either not found inside the default properties or found having an invalid value");
        }
        LOGGER.debug("Found the property [{}] default value [{}]", propertyName, defaultValue);
        return defaultValue;
    }

    @Override
    public void setPropertyValue(Domain domain, String propertyName, String propertyValue) {
        String propertyKey = propertyName;
        if (domain != null && !DomainService.DEFAULT_DOMAIN.equals(domain)) {
            propertyKey = getPropertyName(domain, propertyName);
        }
        this.domibusProperties.setProperty(propertyKey, propertyValue);
    }

}
