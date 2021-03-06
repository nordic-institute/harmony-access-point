
package eu.domibus.plugin.fs.property;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.exceptions.DomibusPropertyExtException;
import eu.domibus.ext.services.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.property.listeners.TriggerChangeListener;
import eu.domibus.plugin.fs.worker.FSSendMessagesService;
import eu.domibus.plugin.property.PluginPropertyChangeNotifier;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.*;
import static eu.domibus.plugin.fs.worker.FSSendMessagesService.DEFAULT_DOMAIN;

/**
 * File System Plugin Properties
 * <p>
 * All the plugin configurable properties must be accessed and handled through this component.
 *
 * @author @author FERNANDES Henrique, GONCALVES Bruno
 */
@Service
public class FSPluginProperties extends DomibusPropertyExtServiceDelegateAbstract implements DomibusPropertyManagerExt {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSPluginProperties.class);

    private static final String DOT = ".";

    private static final String DEFAULT_CONTENT_ID = "cid:message";

    @Resource(name = "fsPluginProperties")
    private Properties properties;

    @Autowired
    protected PasswordEncryptionExtService pluginPasswordEncryptionService;

    @Autowired
    protected DomibusConfigurationExtService domibusConfigurationExtService;

    @Autowired
    protected FSPluginPropertiesMetadataManagerImpl fsPluginPropertiesMetadataManager;

    @Autowired
    protected DomainContextExtService domainContextProvider;

    @Autowired
    @Lazy
    PluginPropertyChangeNotifier pluginPropertyChangeNotifier;

    private List<String> domains;

    protected Map<String, DomibusPropertyMetadataDTO> knownProperties;

    public static final String ACTION_DELETE = "delete";

    public static final String ACTION_ARCHIVE = "archive";

    /**
     * @return The available domains set
     */
    public List<String> getDomains() {
        if (domains == null) {
            domains = readDomains();
        }
        return domains;
    }

    public void resetDomains() {
        LOG.debug("Resetting domains");
        domains = null;
    }

    /**
     * @param domain The domain property qualifier
     * @return The location of the directory that the plugin will use to manage the messages to be sent and received
     */
    public String getLocation(String domain) {
        return getDomainProperty(domain, LOCATION, System.getProperty("java.io.tmpdir"));
    }

    /**
     * @param domain The domain property qualifier
     * @return The plugin action when message is sent successfully from C2 to C3 ('delete' or 'archive')
     */
    public String getSentAction(String domain) {
        return getDomainProperty(domain, SENT_ACTION, ACTION_DELETE);
    }

    /**
     * @return The cron expression that defines the frequency of the sent messages purge job
     */
    public String getSentPurgeWorkerCronExpression(String domain) {
        return getDomainProperty(domain, SENT_PURGE_WORKER_CRONEXPRESSION, null);
    }

    /**
     * @param domain The domain property qualifier
     * @return The time interval (seconds) to purge sent messages or 0 for purge disabled
     */
    public int getSentPurgeExpired(String domain) {
        String value = getDomainProperty(domain, SENT_PURGE_EXPIRED, "600");
        if(StringUtils.isBlank(value)){
            return 0;
        }
        return NumberUtils.toInt(value);
    }

    /**
     * Gets the threshold value that will be used to schedule payloads for async saving
     *
     * @param domain The domain for which the value will be retrieved
     * @return The threshold value in MB
     */
    public Long getPayloadsScheduleThresholdMB(String domain) {
        String value = getDomainProperty(domain, PAYLOAD_SCHEDULE_THRESHOLD, "1000");
        return NumberUtils.toLong(value);
    }

    /**
     * @param domain The domain property qualifier
     * @return The plugin action when message fails
     */
    public String getFailedAction(String domain) {
        return getDomainProperty(domain, FAILED_ACTION, ACTION_DELETE);
    }

    /**
     * @return The cron expression that defines the frequency of the failed messages purge job
     */
    public String getFailedPurgeWorkerCronExpression(String domain) {
        return getDomainProperty(domain, FAILED_PURGE_WORKER_CRONEXPRESSION, null);
    }

    /**
     * @return The cron expression that defines the frequency of the received messages purge job
     */
    public String getReceivedPurgeWorkerCronExpression(String domain) {
        return getDomainProperty(domain, RECEIVED_PURGE_WORKER_CRONEXPRESSION, null);
    }

    /**
     * @return The cron expression that defines the frequency of the orphan lock files purge job
     */
    public String getLocksPurgeWorkerCronExpression(String domain) {
        return getDomainProperty(domain, LOCKS_PURGE_WORKER_CRONEXPRESSION, null);
    }

    /**
     * @param domain The domain property qualifier
     * @return The time interval (seconds) to purge failed messages or 0 for purge disabled
     */
    public int getFailedPurgeExpired(String domain) {
        String value = getDomainProperty(domain, FAILED_PURGE_EXPIRED, "600");
        if(StringUtils.isBlank(value)){
            return 0;
        }
        return NumberUtils.toInt(value);
    }

    /**
     * @param domain The domain property qualifier
     * @return The time interval (seconds) to purge received messages or 0 for purge disabled
     */
    public int getReceivedPurgeExpired(String domain) {
        String value = getDomainProperty(domain, RECEIVED_PURGE_EXPIRED, "600");
        if(StringUtils.isBlank(value)){
            return 0;
        }
        return NumberUtils.toInt(value);
    }

    /**
     * @param domain The domain property qualifier
     * @return The time interval (seconds) to purge orphan lock files or 0 for purge disabled
     */
    public int getLocksPurgeExpired(String domain) {
        String value = getDomainProperty(domain, LOCKS_PURGE_EXPIRED, "600");
        if(StringUtils.isBlank(value)){
            return 0;
        }
        return NumberUtils.toInt(value);
    }

    /**
     * @param domain The domain property qualifier
     * @return the user used to access the location specified by the property
     */
    public String getUser(String domain) {
        return getDomainProperty(domain, USER, null);
    }

    /**
     * Returns the payload identifier for messages belonging to a particular domain or the default payload identifier if none is defined.
     *
     * @param domain the domain property qualifier; {@code null} for the non-multitenant default domain
     * @return The identifier used to reference payloads of messages belonging to a particular domain.
     */
    public String getPayloadId(String domain) {
        return getDomainProperty(domain, PAYLOAD_ID, DEFAULT_CONTENT_ID);
    }

    /**
     * @param domain The domain property qualifier
     * @return the password used to access the location specified by the property
     */
    public String getPassword(String domain) {
        String result = getDomainProperty(domain, PASSWORD, null);
        if (pluginPasswordEncryptionService.isValueEncrypted(result)) {
            LOG.debug("Decrypting property [{}] for domain [{}]", PASSWORD, domain);
            //passwords are encrypted using the key of the default domain; this is because there is no clear segregation between FS Plugin properties per domain
            final DomainDTO domainDTO = domainExtService.getDomain(FSSendMessagesService.DEFAULT_DOMAIN);
            result = pluginPasswordEncryptionService.decryptProperty(domainDTO, PASSWORD, result);
        }
        return result;
    }

    /**
     * @param domain The domain property qualifier
     * @return the user used to authenticate
     */
    public String getAuthenticationUser(String domain) {
        return getDomainPropertyNoDefault(domain, AUTHENTICATION_USER, null);
    }

    /**
     * @param domain The domain property qualifier
     * @return the password used to authenticate
     */
    public String getAuthenticationPassword(String domain) {
        String result = getDomainPropertyNoDefault(domain, AUTHENTICATION_PASSWORD, null);
        if (pluginPasswordEncryptionService.isValueEncrypted(result)) {
            LOG.debug("Decrypting property [{}] for domain [{}]", AUTHENTICATION_PASSWORD, domain);
            //passwords are encrypted using the key of the default domain; this is because there is no clear segregation between FS Plugin properties per domain
            final DomainDTO domainDTO = domainExtService.getDomain(FSSendMessagesService.DEFAULT_DOMAIN);
            result = pluginPasswordEncryptionService.decryptProperty(domainDTO, AUTHENTICATION_PASSWORD, result);
        }
        return result;
    }

    /**
     * @param domain The domain property qualifier
     * @return the domain order
     */
    public Integer getOrder(String domain) {
        String value = getDomainProperty(domain, ORDER, null);
        return getInteger(value, Integer.MAX_VALUE);
    }

    Properties getProperties() {
        return properties;
    }

    void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * @param domain The domain property qualifier
     * @return the regex expression used to determine the domain location
     */
    public String getExpression(String domain) {
        return getDomainProperty(domain, EXPRESSION, null);
    }

    /**
     * FSPluginOut queue concurrency
     *
     * @param domain the domain
     * @return concurrency value
     */
    public String getMessageOutQueueConcurrency(final String domain) {
        return getDomainProperty(domain, OUT_QUEUE_CONCURRENCY, "5-10");
    }

    /**
     * @param domain The domain property qualifier
     * @return delay value in milliseconds
     */
    public Integer getSendDelay(String domain) {
        String value = getDomainProperty(domain, SEND_DELAY, null);
        return getInteger(value, 2000);
    }

    /**
     * @param domain The domain property qualifier
     * @return send worker interval in milliseconds
     */
    public Integer getSendWorkerInterval(String domain) {
        String value = getDomainProperty(domain, SEND_WORKER_INTERVAL, null);
        return getInteger(value, 10000);
    }

    /**
     * @param domain The domain property qualifier
     * @return True if the sent messages action is "archive"
     */
    public boolean isSentActionArchive(String domain) {
        return ACTION_ARCHIVE.equals(getSentAction(domain));
    }

    /**
     * @param domain The domain property qualifier
     * @return True if the sent messages action is "delete"
     */
    public boolean isSentActionDelete(String domain) {
        return ACTION_DELETE.equals(getSentAction(domain));
    }

    /**
     * @param domain The domain property qualifier
     * @return True if the send failed messages action is "archive"
     */
    public boolean isFailedActionArchive(String domain) {
        return ACTION_ARCHIVE.equals(getFailedAction(domain));
    }

    /**
     * @param domain The domain property qualifier
     * @return True if the send failed messages action is "delete"
     */
    public boolean isFailedActionDelete(String domain) {
        return ACTION_DELETE.equals(getFailedAction(domain));
    }

    /**
     * @return True if password encryption is active
     */
    public boolean isPasswordEncryptionActive() {
        final String passwordEncryptionActive = getDomainPropertyNoDefault(DEFAULT_DOMAIN, PASSWORD_ENCRYPTION_ACTIVE, "false");
        return BooleanUtils.toBoolean(passwordEncryptionActive);
    }


    public String getDomainProperty(String domain, String propertyName, String defaultValue) {
        String domainFullPropertyName = getDomainPropertyName(domain, propertyName);
        if (properties.containsKey(domainFullPropertyName)) {
            return properties.getProperty(domainFullPropertyName, defaultValue);
        }
        return properties.getProperty(PROPERTY_PREFIX + propertyName, defaultValue);
    }

    public String getDomainPropertyNoDefault(String domain, String propertyName, String defaultValue) {
        String domainFullPropertyName = getDomainPropertyName(domain, propertyName);
        if (properties.containsKey(domainFullPropertyName)) {
            return properties.getProperty(domainFullPropertyName, defaultValue);
        }
        if (DEFAULT_DOMAIN.equals(domain)) {
            if (!StringUtils.startsWith(propertyName, PROPERTY_PREFIX)) {
                propertyName = PROPERTY_PREFIX + propertyName;
            }
            return properties.getProperty(propertyName, defaultValue);
        }
        // cannot use default values
        return null;
    }

    private Integer getInteger(String value, Integer defaultValue) {
        Integer result = defaultValue;
        if (StringUtils.isNotEmpty(value)) {
            result = Integer.valueOf(value);
        }
        return result;
    }

    protected List<String> readDomains() {
        List<String> tempDomains = new ArrayList<>();

        for (String propName : properties.stringPropertyNames()) {
            if (propName.startsWith(DOMAIN_PREFIX)) {
                String domain = extractDomainName(propName);
                if (!tempDomains.contains(domain)) {
                    tempDomains.add(domain);
                }
            }
        }
        if (!tempDomains.contains(DEFAULT_DOMAIN)) {
            tempDomains.add(DEFAULT_DOMAIN);
        }

        Collections.sort(tempDomains, (domain1, domain2) -> {
            Integer domain1Order = getOrder(domain1);
            Integer domain2Order = getOrder(domain2);
            return domain1Order - domain2Order;
        });
        return tempDomains;
    }

    private String getDomainPropertyName(String domain, String propertyName) {
        if (domain == null) {
            return propertyName;
        }
        String basePropertyName = getBasePropertyName(propertyName);
        return DOMAIN_PREFIX + domain + DOT + basePropertyName;
    }

    private String extractDomainName(String propName) {
        String unPrefixedProp = StringUtils.removeStart(propName, DOMAIN_PREFIX);
        return StringUtils.substringBefore(unPrefixedProp, DOT);
    }

    private String getBasePropertyName(String propName) {
        String baseName = propName;
        if (baseName.startsWith(DOMAIN_PREFIX)) { // fsplugin.domains.x.baseName
            LOG.trace("Processing a domain property name [{}]", baseName);
            baseName = StringUtils.removeStart(baseName, DOMAIN_PREFIX);
            baseName = StringUtils.substringAfter(baseName, DOT); // remove the domain name
        } else if (baseName.startsWith(PROPERTY_PREFIX)) { // fsplugin.baseName
            LOG.trace("Processing a non-domain property name [{}]", baseName);
            baseName = StringUtils.removeStart(baseName, PROPERTY_PREFIX);
        }
        return baseName;
    }

    @Override
    public synchronized Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        if (knownProperties != null) {
            return knownProperties;
        }

        knownProperties = new HashMap<>();

        Map<String, DomibusPropertyMetadataDTO> baseProperties = fsPluginPropertiesMetadataManager.getKnownProperties();

        // in multi-tenancy mode - we only expose the "base" properties from the current domain
        if (domibusConfigurationExtService.isMultiTenantAware()) {
            updatePropertiesForMultitenancy(baseProperties);
            return knownProperties;
        }

        //single tenancy mode
        updatePropertiesForSingletenancy(baseProperties);
        return knownProperties;
    }

    protected void updatePropertiesForSingletenancy(Map<String, DomibusPropertyMetadataDTO> baseProperties) {
        for (DomibusPropertyMetadataDTO propMeta : baseProperties.values()) {
            if (shouldMultiplyPropertyMetadata(propMeta)) {
                LOG.debug("Multiplying the domain property [{}] for each domain.", propMeta.getName());
                for (String domain : getDomains()) {
                    String name = getDomainPropertyName(domain, propMeta.getName());
                    DomibusPropertyMetadataDTO propertyMetadata = new DomibusPropertyMetadataDTO(name, propMeta.getType(), Module.FS_PLUGIN, DomibusPropertyMetadataDTO.Usage.DOMAIN, propMeta.isWithFallback());
                    propertyMetadata.setStoredGlobally(propMeta.isStoredGlobally());
                    knownProperties.put(propertyMetadata.getName(), propertyMetadata);
                }
            } else {
                updatePropertyName(propMeta);
                //if not multiplied, the usage should be global
                propMeta.setUsage(DomibusPropertyMetadataDTO.Usage.GLOBAL);
                knownProperties.put(propMeta.getName(), propMeta);
            }
        }
    }

    protected void updatePropertiesForMultitenancy(Map<String, DomibusPropertyMetadataDTO> baseProperties) {
        for (DomibusPropertyMetadataDTO propMeta : baseProperties.values()) {
            updatePropertyName(propMeta);
            knownProperties.put(propMeta.getName(), propMeta);
        }
    }

    private void updatePropertyName(DomibusPropertyMetadataDTO propMeta) {
        String name = propMeta.getName().contains(PROPERTY_PREFIX) ? propMeta.getName() : PROPERTY_PREFIX + propMeta.getName();
        propMeta.setName(name);
        LOG.debug("Update the simple property name [{}] of the known property list.", name);
    }

    @Override
    public boolean hasKnownProperty(String name) {
        return this.getKnownProperties().containsKey(name);
    }

    public String getKnownPropertyValue(String domainCode, String propertyName) {
        if (!hasKnownProperty(propertyName)) {
            throw new DomibusPropertyExtException("Unknown property name: " + propertyName);
        }

        // propertyName may or may not already include the domaincode (in single-tenancy vs multi-tenancy)
        if (propertyName.startsWith(DOMAIN_PREFIX) && this.properties.containsKey(propertyName)) {
            return this.properties.getProperty(propertyName);
        }
        String baseName = getBasePropertyName(propertyName);
        String key1 = DOMAIN_PREFIX + domainCode + DOT + baseName;
        String key2 = PROPERTY_PREFIX + baseName;

        if (this.properties.containsKey(key1)) {
            return this.properties.getProperty(key1);
        }
        if (this.properties.containsKey(key2)) {
            return this.properties.getProperty(key2);
        }
        return null;
    }

    @Override
    public String getKnownPropertyValue(String propertyName) {
        DomainDTO currentDomain = domainContextProvider.getCurrentDomainSafely();
        String domainCode = currentDomain == null ? null : currentDomain.getCode();
        return getKnownPropertyValue(domainCode, propertyName);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast) {
        LOG.debug("Updating value of property [{}] on domain [{}]", propertyName, domainCode);
        if (!hasKnownProperty(propertyName)) {
            throw new DomibusPropertyExtException("Unknown property name: " + propertyName);
        }

        String propertyKey = propertyName;
        if (domibusConfigurationExtService.isMultiTenantAware()) {
            propertyKey = getDomainPropertyName(domainCode, propertyName);
        }
        this.properties.setProperty(propertyKey, propertyValue);

        LOG.debug("Signaling property value changed for [{}] property, broadcast: [{}]", propertyName, broadcast);
        pluginPropertyChangeNotifier.signalPropertyValueChanged(domainCode, propertyName, propertyValue, broadcast);
    }

    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue) {
        setKnownPropertyValue(domainCode, propertyName, propertyValue, true);
    }

    @Override
    public void setKnownPropertyValue(String propertyName, String propertyValue) {
        DomainDTO currentDomain = domainContextProvider.getCurrentDomainSafely();
        String domainCode = currentDomain == null ? null : currentDomain.getCode();
        setKnownPropertyValue(domainCode, propertyName, propertyValue);
    }

    protected boolean shouldMultiplyPropertyMetadata(DomibusPropertyMetadataDTO propMeta) {
        // in single-domain mode - we only expose the "base" properties
        // in fsplugin's custom multi-domain mode, in single-tenancy - we expose each "base" property once per every domain
        return getDomains().size() > 1
                && propMeta.isDomain()
                // we do not multiply properties used for quartz jobs
                && !isQuartzRelated(propMeta);
    }

    private boolean isQuartzRelated(DomibusPropertyMetadataDTO propMeta) {
        return TriggerChangeListener.CRON_PROPERTY_NAMES_TO_JOB_MAP.keySet().stream().anyMatch(key -> key.contains(propMeta.getName()));
    }
}
