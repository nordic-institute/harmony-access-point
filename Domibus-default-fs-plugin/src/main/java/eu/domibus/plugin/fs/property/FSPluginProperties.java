
package eu.domibus.plugin.fs.property;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.exceptions.DomibusPropertyExtException;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.ext.services.DomibusPropertyExtServiceDelegateAbstract;
import eu.domibus.ext.services.PasswordEncryptionExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.property.listeners.TriggerChangeListener;
import eu.domibus.plugin.fs.worker.FSSendMessagesService;
import eu.domibus.plugin.property.PluginPropertyChangeNotifier;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.*;
import static eu.domibus.plugin.fs.worker.FSSendMessagesService.DEFAULT_DOMAIN;

/**
 * File System Plugin Properties
 * <p>
 * All the plugin configurable properties must be accessed and handled through this component.
 *
 * @author FERNANDES Henrique
 * @author GONCALVES Bruno
 */
@Service
@PropertySource(value = "file:///${domibus.config.location}/plugins/config/fs-plugin.properties")
public class FSPluginProperties extends DomibusPropertyExtServiceDelegateAbstract {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSPluginProperties.class);

    private static final String DOT = ".";

    private static final String DEFAULT_CONTENT_ID = "cid:message";

    @Value("${fsplugin.domains.list:}")//still keeping this for reading fs plugin domain list
    private String fsPluginDomainsList;

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

    private volatile List<String> domains;

    protected final Object domainsLock = new Object();

    protected Map<String, DomibusPropertyMetadataDTO> knownProperties;

    public static final String ACTION_DELETE = "delete";

    public static final String ACTION_ARCHIVE = "archive";


    /**
     * Get the domain list
     * @return The available domains set
     */
    public List<String> getDomains() {
        if (domains == null) {
            synchronized (domainsLock) {
                if (domains == null) {
                    domains = readDomains();
                }
            }
        }
        return domains;
    }

    public void resetDomains() {
        LOG.debug("Resetting domains");
        synchronized (domainsLock) {
            domains = null;
        }
    }

    protected List<String> readDomains() {
        List<String> tempDomains = new ArrayList<>();

        //getting domains list
        String domainsListStr = fsPluginDomainsList;
        if (StringUtils.isNotEmpty(domainsListStr)) {
            List<String> fsPluginDomains = Stream.of(domainsListStr.split(","))
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.toList());
            LOG.debug("The following domains were found [{}]", fsPluginDomains);
            tempDomains.addAll(fsPluginDomains);
        }

        if (!tempDomains.contains(DEFAULT_DOMAIN)) {
            tempDomains.add(DEFAULT_DOMAIN);
        }

        return tempDomains;
    }
    /**
     * @param domain The domain property qualifier
     * @return The location of the directory that the plugin will use to manage the messages to be sent and received
     */
    public String getLocation(String domain) {
        String value = getDomainProperty(domain, LOCATION);
        if (StringUtils.isBlank(value)) {
            String tmpFolder = System.getProperty("java.io.tmpdir");
            LOG.warn("[{}] property not set for domain=[{}] going to use [{}]", LOCATION, domain, tmpFolder);
            return tmpFolder;
        }
        return value;
    }

    /**
     * @param domain The domain property qualifier
     * @return The plugin action when message is sent successfully from C2 to C3 ('delete' or 'archive')
     */
    public String getSentAction(String domain) {
        return getDomainProperty(domain, SENT_ACTION);
    }

    /**
     * @return The cron expression that defines the frequency of the sent messages purge job
     */
    public String getSentPurgeWorkerCronExpression(String domain) {
        return super.getKnownPropertyValue(SENT_PURGE_WORKER_CRONEXPRESSION);
    }

    /**
     * @param domain The domain property qualifier
     * @return The time interval (seconds) to purge sent messages
     */
    public Integer getSentPurgeExpired(String domain) {
        return getIntegerDomainProperty(domain, SENT_PURGE_EXPIRED);
    }


    /**
     * Gets the threshold value that will be used to schedule payloads for async saving
     *
     * @return The threshold value in MB
     */
    public Long getPayloadsScheduleThresholdMB() {
        String value = super.getKnownPropertyValue(PAYLOAD_SCHEDULE_THRESHOLD);
        return NumberUtils.toLong(value);
    }

    /**
     * @param domain The domain property qualifier
     * @return The plugin action when message fails
     */
    public String getFailedAction(String domain) {
        return getDomainProperty(domain, FAILED_ACTION);
    }

    /**
     * @return The cron expression that defines the frequency of the failed messages purge job
     */
    public String getFailedPurgeWorkerCronExpression() {
        return super.getKnownPropertyValue(FAILED_PURGE_WORKER_CRONEXPRESSION);
    }

    /**
     * @return The cron expression that defines the frequency of the received messages purge job
     */
    public String getReceivedPurgeWorkerCronExpression() {
        return super.getKnownPropertyValue(RECEIVED_PURGE_WORKER_CRONEXPRESSION);
    }

    /**
     * @return The cron expression that defines the frequency of the orphan lock files purge job
     */
    public String getLocksPurgeWorkerCronExpression() {
        return super.getKnownPropertyValue(LOCKS_PURGE_WORKER_CRONEXPRESSION);
    }

    /**
     * @param domain The domain property qualifier
     * @return The time interval (seconds) to purge failed messages
     */
    public Integer getFailedPurgeExpired(String domain) {
        return getIntegerDomainProperty(domain, FAILED_PURGE_EXPIRED);
    }

    /**
     * @param domain The domain property qualifier
     * @return The time interval (seconds) to purge received messages
     */
    public Integer getReceivedPurgeExpired(String domain) {
        return getIntegerDomainProperty(domain, RECEIVED_PURGE_EXPIRED);
    }

    /**
     * @param domain The domain property qualifier
     * @return The time interval (seconds) to purge orphan lock files
     */
    public Integer getLocksPurgeExpired(String domain) {
        return getIntegerDomainProperty(domain, LOCKS_PURGE_EXPIRED);
    }

    /**
     * @param domain The domain property qualifier
     * @return the user used to access the location specified by the property
     */
    public String getUser(String domain) {
        return getDomainProperty(domain, USER);
    }

    /**
     * Returns the payload identifier for messages belonging to a particular domain or the default payload identifier if none is defined.
     *
     * @param domain the domain property qualifier; {@code null} for the non-multitenant default domain
     * @return The identifier used to reference payloads of messages belonging to a particular domain.
     */
    public String getPayloadId(String domain) {
        return getDomainProperty(domain, PAYLOAD_ID);
    }

    /**
     * @param domain The domain property qualifier
     * @return the password used to access the location specified by the property
     */
    public String getPassword(String domain) {
        String result = getDomainProperty(domain, PASSWORD);
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
        return getDomainProperty(domain, AUTHENTICATION_USER);
    }

    /**
     * @param domain The domain property qualifier
     * @return the password used to authenticate
     */
    public String getAuthenticationPassword(String domain) {
        String result = getDomainProperty(domain, AUTHENTICATION_PASSWORD);
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
        String value = getDomainProperty(domain, ORDER);
        return getInteger(value, Integer.MAX_VALUE);
    }

    /**
     * @param domain The domain property qualifier
     * @return the regex expression used to determine the domain location
     */
    public String getExpression(String domain) {
        return getDomainProperty(domain, EXPRESSION);
    }

    /**
     * FSPluginOut queue concurrency
     *
     * @param domain the domain
     * @return concurrency value
     */
    public String getMessageOutQueueConcurrency(final String domain) {
        return getDomainProperty(domain, OUT_QUEUE_CONCURRENCY);
    }

    /**
     * @param domain The domain property qualifier
     * @return delay value in milliseconds
     */
    public Integer getSendDelay(String domain) {
        return getIntegerDomainProperty(domain, SEND_DELAY);
    }

    /**
     * @return send worker interval in milliseconds
     */
    public Integer getSendWorkerInterval() {
        return super.getKnownIntegerPropertyValue(SEND_WORKER_INTERVAL);
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
        final String passwordEncryptionActive = getDomainProperty(DEFAULT_DOMAIN, PASSWORD_ENCRYPTION_ACTIVE);
        return BooleanUtils.toBoolean(passwordEncryptionActive);
    }


    /**
     * get the base (mapped to default) and other domains property
     * @param domain
     * @param propertyName
     * @return
     */
    public String getDomainProperty(String domain, String propertyName) {
        if (domibusConfigurationExtService.isMultiTenantAware()) {
            return super.getKnownPropertyValue(propertyName);
        }
        //ST
        return getDomainPropertyST(domain, propertyName);
    }

    protected String getDomainPropertyST(String domain, String propertyName) {
        String value;

        if (!DEFAULT_DOMAIN.equalsIgnoreCase(domain)) { //FS Plugin domain like properties for ST
            String propertyNameFinal = domain + DOT + propertyName;
            value = super.getKnownPropertyValue(propertyNameFinal);
            DomibusPropertyMetadataDTO propertyMetadataDTO = getKnownProperties().get(propertyNameFinal);

            if (value == null && propertyMetadataDTO.isWithFallback()) { //try to get the value from default properties file
                LOG.debug("going to obtain default value for property [{}] which has fallback", propertyNameFinal);
                value = super.getKnownPropertyValue(propertyName);
                if (StringUtils.isEmpty(value)) {
                    throw new DomibusPropertyExtException("FSPlugin property [" + propertyNameFinal + "] is empty or not present in fs-plugin.properties file");
                }
            }
        } else {
            //default domain
            value = super.getKnownPropertyValue(propertyName);
        }

        return value;
    }

    public Integer getIntegerDomainProperty(String domain, String propertyName) {
        String value = getDomainProperty(domain, propertyName);
        return NumberUtils.toInt(value);
    }

//    public String getDomainPropertyNoDefault(String domain, String propertyName) {
//
//        // cannot use default values
//        return getDomainProperty(domain, propertyName);
//    }

    private Integer getInteger(String value, Integer defaultValue) {
        Integer result = defaultValue;
        if (StringUtils.isNotEmpty(value)) {
            result = Integer.valueOf(value);
        }
        return result;
    }


    //TODO enachca
    protected void sortDomains() {
        Collections.sort(getDomains(), (domain1, domain2) -> {
            Integer domain1Order = getOrder(domain1);
            Integer domain2Order = getOrder(domain2);
            return domain1Order - domain2Order;
        });
    }



    @Override
    public synchronized Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        if (knownProperties != null) {
            return knownProperties;
        }
        knownProperties = new HashMap<>();

        Map<String, DomibusPropertyMetadataDTO> baseProperties = fsPluginPropertiesMetadataManager.getKnownProperties();

       //  in multi-tenancy mode - we only expose the "base" properties from the current domain
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
                        String name = (DEFAULT_DOMAIN.equals(domain) ? StringUtils.EMPTY : domain + DOT) + propMeta.getName();
                        DomibusPropertyMetadataDTO propertyMetadata = new DomibusPropertyMetadataDTO(name, propMeta.getType(), Module.FS_PLUGIN, DomibusPropertyMetadataDTO.Usage.DOMAIN, propMeta.isWithFallback());
                        propertyMetadata.setStoredGlobally(true/*propMeta.isStoredGlobally()*/);
                        knownProperties.put(propertyMetadata.getName(), propertyMetadata);
                }
            } else {
                //if not multiplied, the usage should be global
                //TODO enachca check why?
                propMeta.setUsage(DomibusPropertyMetadataDTO.Usage.GLOBAL);
                knownProperties.put(propMeta.getName(), propMeta);
            }
        }
    }

    protected void updatePropertiesForMultitenancy(Map<String, DomibusPropertyMetadataDTO> baseProperties) {
        for (DomibusPropertyMetadataDTO propMeta : baseProperties.values()) {
            //updatePropertyName(propMeta);
            knownProperties.put(propMeta.getName(), propMeta);
        }
    }

//    private void updatePropertyName(DomibusPropertyMetadataDTO propMeta) {
//        String name = propMeta.getName().contains(PROPERTY_PREFIX) ? propMeta.getName() : PROPERTY_PREFIX + propMeta.getName();
//        propMeta.setName(name);
//        LOG.debug("Update the simple property name [{}] of the known property list.", name);
//    }

//    @Override
//    public boolean hasKnownProperty(String name) {
//        return this.getKnownProperties().containsKey(name);
//    }

//    @Override
//    public String getKnownPropertyValue(String domainCode, String propertyName) {
//        if (!hasKnownProperty(propertyName)) {
//            throw new DomibusPropertyExtException("Unknown property name: " + propertyName);
//        }
//
//        if (domibusConfigurationExtService.isMultiTenantAware()) {
//            //MT
//        } else {
//            //ST
//        }
//
//
//        // propertyName may or may not already include the domaincode (in single-tenancy vs multi-tenancy)
//        if (propertyName.startsWith(DOMAIN_PREFIX) && this.properties.containsKey(propertyName)) {
//            return this.properties.getProperty(propertyName);
//        }
//        String baseName = getBasePropertyName(propertyName);
//        String key1 = DOMAIN_PREFIX + domainCode + DOT + baseName;
//        String key2 = PROPERTY_PREFIX + baseName;
//
//        if (this.properties.containsKey(key1)) {
//            return this.properties.getProperty(key1);
//        }
//        if (this.properties.containsKey(key2)) {
//            return this.properties.getProperty(key2);
//        }
//        return null;
//    }

//    @Override
//    public String getKnownPropertyValue(String propertyName) {
//        DomainDTO currentDomain = domainContextProvider.getCurrentDomainSafely();
//        String domainCode = currentDomain == null ? null : currentDomain.getCode();
//        return getKnownPropertyValue(domainCode, propertyName);
//    }

//    @Override
//    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast) {
//        LOG.debug("Updating value of property [{}] on domain [{}]", propertyName, domainCode);
//        if (!hasKnownProperty(propertyName)) {
//            throw new DomibusPropertyExtException("Unknown property name: " + propertyName);
//        }
//
//        String propertyKey = propertyName;
//        if (domibusConfigurationExtService.isMultiTenantAware()) {
//            propertyKey = getDomainPropertyName(domainCode, propertyName);
//        }
//        this.properties.setProperty(propertyKey, propertyValue);
//
//        LOG.debug("Signaling property value changed for [{}] property, broadcast: [{}]", propertyName, broadcast);
//        pluginPropertyChangeNotifier.signalPropertyValueChanged(domainCode, propertyName, propertyValue, broadcast);
//    }
//
//    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue) {
//        setKnownPropertyValue(domainCode, propertyName, propertyValue, true);
//    }

//    @Override
//    public void setKnownPropertyValue(String propertyName, String propertyValue) {
//        DomainDTO currentDomain = domainContextProvider.getCurrentDomainSafely();
//        String domainCode = currentDomain == null ? null : currentDomain.getCode();
//        setKnownPropertyValue(domainCode, propertyName, propertyValue);
//    }

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
