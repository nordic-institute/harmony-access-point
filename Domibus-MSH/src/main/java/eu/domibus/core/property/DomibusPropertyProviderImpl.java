package eu.domibus.core.property;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.property.*;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Cosmin Baciu, Ion Perpegel
 * @since 4.0
 */
@Service
public class DomibusPropertyProviderImpl implements DomibusPropertyProvider, DomibusPropertyManager {

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
    protected DomibusConfigurationService domibusConfigurationService;

    @Autowired
    protected DomainService domainService;

    //TODO: move out from here
    @Autowired
    List<DomibusPropertyChangeListener> domibusPropertyChangeListeners;

    @Autowired
    protected ApplicationContext applicationContext;

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusPropertyProviderImpl.class);

    protected String getPropertyName(Domain domain, String propertyName) {
        return domain.getCode() + "." + propertyName;
    }

    @Override
    public String getProperty(Domain domain, String propertyName) {
        final String domainPropertyName = getPropertyName(domain, propertyName);
        String propertyValue = getPropertyValue(domainPropertyName);
        if (StringUtils.isEmpty(propertyValue) && DomainService.DEFAULT_DOMAIN.equals(domain)) {
            propertyValue = getPropertyValue(propertyName);
        }
        return propertyValue;
    }

    /**
     * Get the value from the system properties; if not found get the value from Domibus properties; if still not found, look inside the Domibus default properties.
     *
     * @param propertyName the property name
     * @return The value of the property as found in the system properties, the Domibus properties or inside the default Domibus properties.
     */
    protected String getPropertyValue(String propertyName) {
        String result = System.getenv(propertyName);
        if (StringUtils.isEmpty(result)) {
            result = domibusProperties.getProperty(propertyName);

            // There is no need to retrieve the default Domibus property value here since the Domibus properties above will contain it, unless overwritten by users.
            // For String property values, if users have overwritten their original default Domibus property values, it is their responsibility to ensure they are valid.
            // For all the other Boolean and Integer property values, if users have overwritten their original default Domibus property values, they are defaulted back to their
            // original default Domibus values when invalid (please check the #getInteger..(..) and #getBoolean..(..) methods below).
        }
        return result;
    }

    @Override
    public String getProperty(String propertyName) {
        return getProperty(DomainService.DEFAULT_DOMAIN, propertyName);
    }

    @Override
    public String getResolvedProperty(Domain domain, String propertyName) {
        final String domainPropertyName = getPropertyName(domain, propertyName);
        String resolvedProperty = propertyResolver.getResolvedProperty(domainPropertyName, domibusProperties, true);
        if (StringUtils.isEmpty(resolvedProperty) && DomainService.DEFAULT_DOMAIN.equals(domain)) {
            resolvedProperty = propertyResolver.getResolvedProperty(propertyName, domibusProperties, true);
        }
        return resolvedProperty;
    }

    @Override
    public String getResolvedProperty(String propertyName) {
        return getResolvedProperty(DomainService.DEFAULT_DOMAIN, propertyName);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDomainProperty(String propertyName) {
        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        if (currentDomain == null) {
            currentDomain = DomainService.DEFAULT_DOMAIN;
        }
        return getDomainProperty(currentDomain, propertyName);
    }

    /**
     * Retrieves the property value from the requested domain.
     * If not found, fall back to the property value from the default domain.
     */
    @Override
    public String getDomainProperty(Domain domain, String propertyName) {
        String propertyValue = getProperty(domain, propertyName);
        if (StringUtils.isEmpty(propertyValue) && !DomainService.DEFAULT_DOMAIN.equals(domain)) {
            propertyValue = getProperty(DomainService.DEFAULT_DOMAIN, propertyName);
        }
        return propertyValue;
    }

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

    @Override
    public Integer getIntegerDomainProperty(String propertyName) {
        String domainValue = getDomainProperty(propertyName);
        return getIntegerInternal(propertyName, domainValue);
    }

    @Override
    public Integer getIntegerDomainProperty(Domain domain, String propertyName) {
        String domainValue = getDomainProperty(domain, propertyName);
        return getIntegerInternal(propertyName, domainValue);
    }

    @Override
    public Long getLongDomainProperty(Domain domain, String propertyName) {
        String domainValue = getDomainProperty(domain, propertyName);
        return getLongInternal(propertyName, domainValue);
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

    @Override
    public Boolean getBooleanDomainProperty(String propertyName) {
        String domainValue = getDomainProperty(propertyName);
        return getBooleanInternal(propertyName, domainValue);
    }

    @Override
    public Boolean getBooleanDomainProperty(Domain domain, String propertyName) {
        String domainValue = getDomainProperty(domain, propertyName);
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
    public Map<String, DomibusPropertyMetadata> getKnownProperties() {
        // TODO
        return Arrays.stream(new DomibusPropertyMetadata[]{
                new DomibusPropertyMetadata("domibus.UI.title.name"),
                new DomibusPropertyMetadata("domibus.ui.replication.enabled"),
                new DomibusPropertyMetadata("domibus.ui.support.team.name"),
                new DomibusPropertyMetadata("domibus.ui.support.team.email"),

                new DomibusPropertyMetadata("domibus.security.keystore.location", PropertyUsageType.DOMAIN_PROPERTY_RESOLVED),
                new DomibusPropertyMetadata("domibus.security.keystore.type", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),
                new DomibusPropertyMetadata("domibus.security.keystore.password", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),
                new DomibusPropertyMetadata("domibus.security.key.private.alias", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),

                new DomibusPropertyMetadata("domibus.security.truststore.location", PropertyUsageType.DOMAIN_PROPERTY_RESOLVED),
                new DomibusPropertyMetadata("domibus.security.truststore.type", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),
                new DomibusPropertyMetadata("domibus.security.truststore.password", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),

                new DomibusPropertyMetadata("domibus.auth.unsecureLoginAllowed", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),
                new DomibusPropertyMetadata("domibus.console.login.maximum.attempt", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.console.login.suspension.time", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.certificate.revocation.offset", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),
                new DomibusPropertyMetadata("domibus.certificate.crl.excludedProtocols", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),

                new DomibusPropertyMetadata("domibus.plugin.login.maximum.attempt", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),
                new DomibusPropertyMetadata("domibus.plugin.login.suspension.time", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),

                new DomibusPropertyMetadata("domibus.passwordPolicy.pattern", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.passwordPolicy.validationMessage", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.passwordPolicy.expiration", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.passwordPolicy.defaultPasswordExpiration", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.passwordPolicy.warning.beforeExpiration", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.passwordPolicy.dontReuseLast", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.passwordPolicy.checkDefaultPassword", PropertyUsageType.GLOBAL_PROPERTY),

                new DomibusPropertyMetadata("domibus.plugin.passwordPolicy.pattern", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.plugin.passwordPolicy.validationMessage", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.passwordPolicy.plugin.expiration", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.passwordPolicy.plugin.defaultPasswordExpiration", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.passwordPolicy.plugin.dontReuseLast", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),

                //TODO: //in the other place it is called, it has a different usage
                new DomibusPropertyMetadata("domibus.attachment.storage.location", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),

                new DomibusPropertyMetadata("domibus.msh.messageid.suffix", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),

                new DomibusPropertyMetadata("domibus.msh.retry.messageExpirationDelay", PropertyUsageType.GLOBAL_PROPERTY),

                new DomibusPropertyMetadata("domibus.dynamicdiscovery.useDynamicDiscovery", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.smlzone", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dynamicdiscovery.client.specification", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dynamicdiscovery.peppolclient.mode", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dynamicdiscovery.oasisclient.regexCertificateSubjectValidation", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dynamicdiscovery.partyid.responder.role", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dynamicdiscovery.partyid.type", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dynamicdiscovery.transportprofileas4", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),

                new DomibusPropertyMetadata("domibus.listPendingMessages.maxCount", PropertyUsageType.GLOBAL_PROPERTY),
                new DomibusPropertyMetadata("domibus.jms.queue.maxBrowseSize", PropertyUsageType.GLOBAL_PROPERTY), //there is one place at init time that it is not refreshed
                new DomibusPropertyMetadata("domibus.jms.internalQueue.expression", PropertyUsageType.GLOBAL_PROPERTY),

                new DomibusPropertyMetadata("domibus.receiver.certificate.validation.onsending", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.sender.certificate.validation.onsending", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.sender.certificate.validation.onreceiving", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.sender.trust.validation.onreceiving", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.sender.trust.validation.expression", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),
                new DomibusPropertyMetadata("domibus.sender.certificate.subject.check", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.sender.trust.validation.truststore_alias", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.sendMessage.messageIdPattern", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),

                new DomibusPropertyMetadata("domibus.dispatcher.connectionTimeout", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dispatcher.receiveTimeout", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dispatcher.allowChunking", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dispatcher.chunkingThreshold", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dispatcher.concurency", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dispatcher.largeFiles.concurrency", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dispatcher.cacheable", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dispatcher.connection.keepAlive", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),

                //

                new DomibusPropertyMetadata("domain.title", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),
                new DomibusPropertyMetadata("domibus.userInput.blackList", PropertyUsageType.GLOBAL_PROPERTY),
                new DomibusPropertyMetadata("domibus.userInput.whiteList", PropertyUsageType.GLOBAL_PROPERTY),
        }).collect(Collectors.toMap(x -> x.getName(), x -> x));
    }

    @Override
    public String getKnownPropertyValue(String domainCode, String propertyName) {
        DomibusPropertyMetadata meta = this.getKnownProperties().get(propertyName);
        if (meta == null) {
            throw new IllegalArgumentException(propertyName);
        }

        Domain domain = domainCode == null ? null : this.domainService.getDomain(domainCode);

        if (meta.getUsage() == PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK) {
            return this.getDomainProperty(domain, meta.getName());
        } else if (meta.getUsage() == PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK) {
            return this.getProperty(domain, meta.getName());
        } else if (meta.getUsage() == PropertyUsageType.DOMAIN_PROPERTY_RESOLVED) {
            return this.getResolvedProperty(domain, meta.getName());
        } else if (meta.getUsage() == PropertyUsageType.GLOBAL_PROPERTY) {
            return this.getProperty(meta.getName());
        }

        throw new NotImplementedException("Get value for : " + propertyName);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue) {
        DomibusPropertyMetadata meta = this.getKnownProperties().get(propertyName);
        if (meta == null) {
            throw new IllegalArgumentException(propertyName);
        }

        final Domain domain = domainCode == null ? null : domainService.getDomain(domainCode);
        if (domain == null || !domibusConfigurationService.isMultiTenantAware()
                || meta.getUsage() == PropertyUsageType.GLOBAL_PROPERTY) {
            // super property - TODO
            // TODO: handle single tenancy too
            this.domibusProperties.setProperty(propertyName, propertyValue);
        } else {
            String fullPropertyName = propertyName;
            if (!DomainService.DEFAULT_DOMAIN.equals(domain)) {
                fullPropertyName = getPropertyName(domain, propertyName);
            }
            this.domibusProperties.setProperty(fullPropertyName, propertyValue);
        }

        handlePropertyChange(domainCode, propertyName, propertyValue);
    }

    private void handlePropertyChange(String domainCode, String propertyName, String propertyValue) {
        //notify interested listeners that the property changed
        //TODO: shall we filter by module( core, plugins)??
        List<DomibusPropertyChangeListener> listeners = domibusPropertyChangeListeners.stream()
                .filter(listener -> listener.handlesProperty(propertyName))
                .collect(Collectors.toList());
        listeners.forEach(listener -> {
            try {
                listener.propertyValueChanged(domainCode, propertyName, propertyValue);
            } catch (Throwable ex) {
                LOGGER.error("An error occurred while setting property [{}] to [{}] ", propertyName, propertyValue, ex);
            }
        });

        //signal for other nodes
        SignalService signalService = applicationContext.getBean(SignalService.class);
        signalService.signalDomibusPropertyChange(domainCode, propertyName, propertyValue);
    }

    @Override
    public boolean hasKnownProperty(String name) {
        // TODO
        return this.getKnownProperties().containsKey(name);
    }

}
