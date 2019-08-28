
package eu.domibus.plugin.fs.property;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import eu.domibus.ext.services.PasswordEncryptionExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.property.PluginPropertyChangeNotifier;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static eu.domibus.plugin.fs.worker.FSSendMessagesService.DEFAULT_DOMAIN;

/**
 * File System Plugin Properties
 * <p>
 * All the plugin configurable properties must be accessed and handled through this component.
 *
 * @author @author FERNANDES Henrique, GONCALVES Bruno
 */
@Component
public class FSPluginProperties implements DomibusPropertyManagerExt {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(FSPluginProperties.class);

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSPluginProperties.class);

    private static final String DOT = ".";

    private static final String PROPERTY_PREFIX = "fsplugin.";

    private static final String DOMAIN_PREFIX = "fsplugin.domains.";

    private static final String LOCATION = "messages.location";

    private static final String SENT_ACTION = "messages.sent.action";

    public static final String SENT_PURGE_WORKER_CRONEXPRESSION = "messages.sent.purge.worker.cronExpression";

    private static final String SENT_PURGE_EXPIRED = "messages.sent.purge.expired";

    private static final String FAILED_ACTION = "messages.failed.action";

    public static final String FAILED_PURGE_WORKER_CRONEXPRESSION = "messages.failed.purge.worker.cronExpression";

    private static final String FAILED_PURGE_EXPIRED = "messages.failed.purge.expired";

    private static final String RECEIVED_PURGE_EXPIRED = "messages.received.purge.expired";

    public static final String OUT_QUEUE_CONCURRENCY = "send.queue.concurrency";

    private static final String SEND_DELAY = "messages.send.delay";

    public static final String SEND_WORKER_INTERVAL = "messages.send.worker.repeatInterval";

    public static final String RECEIVED_PURGE_WORKER_CRONEXPRESSION = "messages.received.purge.worker.cronExpression";

    private static final String USER = "messages.user";

    private static final String PAYLOAD_ID = "messages.payload.id";

    private static final String DEFAULT_CONTENT_ID = "cid:message";

    // Sonar confuses this constant with an actual password
    @SuppressWarnings("squid:S2068")
    private static final String PASSWORD = "messages.password";

    private static final String AUTHENTICATION_USER = "authentication.user";

    // Sonar confuses this constant with an actual password
    @SuppressWarnings("squid:S2068")
    private static final String AUTHENTICATION_PASSWORD = "authentication.password";

    private static final String EXPRESSION = "messages.expression";

    public static final String ORDER = "order";

    private static final String PAYLOAD_SCHEDULE_THRESHOLD = "messages.payload.schedule.threshold";

    private static final String PASSWORD_ENCRYPTION_ACTIVE = "password.encryption.active";

    @Resource(name = "fsPluginProperties")
    private Properties properties;

    @Autowired
    protected PasswordEncryptionExtService pluginPasswordEncryptionService;

    @Autowired
    protected DomainExtService domainExtService;

    @Autowired
    protected DomibusConfigurationExtService domibusConfigurationExtService;
//
//    @Autowired
//    protected PluginPropertyChangeNotifier propertyChangeNotifier;

    @Autowired
    protected ApplicationContext applicationContext;

    private List<String> domains;

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
    public String getSentPurgeWorkerCronExpression() {
        return properties.getProperty(PROPERTY_PREFIX + SENT_PURGE_WORKER_CRONEXPRESSION);
    }

    /**
     * @param domain The domain property qualifier
     * @return The time interval (seconds) to purge sent messages
     */
    public Integer getSentPurgeExpired(String domain) {
        String value = getDomainProperty(domain, SENT_PURGE_EXPIRED, "600");
        return StringUtils.isNotEmpty(value) ? Integer.parseInt(value) : null;
    }

    /**
     * Gets the threshold value that will be used to schedule payloads for async saving
     *
     * @param domain The domain for which the value will be retrieved
     * @return The threshold value in MB
     */
    public Long getPayloadsScheduleThresholdMB(String domain) {
        String value = getDomainProperty(domain, PAYLOAD_SCHEDULE_THRESHOLD, "1000");
        return Long.parseLong(value);
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
    public String getFailedPurgeWorkerCronExpression() {
        return properties.getProperty(PROPERTY_PREFIX + FAILED_PURGE_WORKER_CRONEXPRESSION);
    }

    /**
     * @param domain The domain property qualifier
     * @return The time interval (seconds) to purge failed messages
     */
    public Integer getFailedPurgeExpired(String domain) {
        String value = getDomainProperty(domain, FAILED_PURGE_EXPIRED, "600");
        return StringUtils.isNotEmpty(value) ? Integer.parseInt(value) : null;
    }

    /**
     * @param domain The domain property qualifier
     * @return The time interval (seconds) to purge received messages
     */
    public Integer getReceivedPurgeExpired(String domain) {
        String value = getDomainProperty(domain, RECEIVED_PURGE_EXPIRED, "600");
        return StringUtils.isNotEmpty(value) ? Integer.parseInt(value) : null;
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
            final DomainDTO domainDTO = domainExtService.getDomain(domain);
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
            final DomainDTO domainDTO = domainExtService.getDomain(domain);
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

    public String getDomainPropertyName(String domain, String propertyName) {
        return DOMAIN_PREFIX + domain + DOT + propertyName;
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

    private String extractDomainName(String propName) {
        String unprefixedProp = StringUtils.removeStart(propName, DOMAIN_PREFIX);
        return StringUtils.substringBefore(unprefixedProp, DOT);
    }


    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        return Arrays.stream(new DomibusPropertyMetadataDTO[]{
                new DomibusPropertyMetadataDTO(SEND_WORKER_INTERVAL, Module.FS_PLUGIN, false, false),
                new DomibusPropertyMetadataDTO(SENT_PURGE_WORKER_CRONEXPRESSION, Module.FS_PLUGIN, false, false),
                new DomibusPropertyMetadataDTO(FAILED_PURGE_WORKER_CRONEXPRESSION, Module.FS_PLUGIN, false, false),
                new DomibusPropertyMetadataDTO(RECEIVED_PURGE_WORKER_CRONEXPRESSION, Module.FS_PLUGIN, false, false),
                // without fallback from the default domain :
                new DomibusPropertyMetadataDTO(AUTHENTICATION_USER, Module.FS_PLUGIN, true, false),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_PASSWORD, Module.FS_PLUGIN, true, false), // TODO: handle encryption
                new DomibusPropertyMetadataDTO(USER, Module.FS_PLUGIN, true, false),
                new DomibusPropertyMetadataDTO(PASSWORD, Module.FS_PLUGIN, true, false), // TODO: handle encryption
                // with fallback from the default domain:
                new DomibusPropertyMetadataDTO(LOCATION, Module.FS_PLUGIN, true, true),
                new DomibusPropertyMetadataDTO(ORDER, Module.FS_PLUGIN, true, true),
                new DomibusPropertyMetadataDTO(SEND_DELAY, Module.FS_PLUGIN, true, true),
                new DomibusPropertyMetadataDTO(PAYLOAD_SCHEDULE_THRESHOLD, Module.FS_PLUGIN, true, true),
                new DomibusPropertyMetadataDTO(SENT_ACTION, Module.FS_PLUGIN, true, true),
                new DomibusPropertyMetadataDTO(FAILED_ACTION, Module.FS_PLUGIN, true, true),
                new DomibusPropertyMetadataDTO(SENT_PURGE_EXPIRED, Module.FS_PLUGIN, true, true),
                new DomibusPropertyMetadataDTO(FAILED_PURGE_EXPIRED, Module.FS_PLUGIN, true, true),
                new DomibusPropertyMetadataDTO(RECEIVED_PURGE_EXPIRED, Module.FS_PLUGIN, true, true),
                new DomibusPropertyMetadataDTO(PAYLOAD_ID, Module.FS_PLUGIN, true, true),
                new DomibusPropertyMetadataDTO(OUT_QUEUE_CONCURRENCY, Module.FS_PLUGIN, true, true),
        }).collect(Collectors.toMap(x -> x.getName(), x -> x));
    }

    @Override
    public boolean hasKnownProperty(String name) {
        return this.getKnownPropertyMetadata(name) != null;
    }

    @Override
    public String getKnownPropertyValue(String domain, String propertyName) {
        String propertyKey = getKnownPropertyKey(domain, propertyName);
        return this.properties.getProperty(propertyKey);
    }

    @Override
    //TODO: reuse same code as in DomibusPropertyManager (EDELIVERY-4812)
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast) {
        String propertyKey = getKnownPropertyKey(domainCode, propertyName);
        this.properties.setProperty(propertyKey, propertyValue);

        PluginPropertyChangeNotifier propertyChangeNotifier = applicationContext.getBean(PluginPropertyChangeNotifier.class);
        propertyChangeNotifier.signalPropertyValueChanged(domainCode, propertyName, propertyValue, broadcast);
    }

    private DomibusPropertyMetadataDTO getKnownPropertyMetadata(String propertyName) {
        return this.getKnownProperties().get(propertyName);
    }

    private String getKnownPropertyKey(String domain, String propertyName) {
        final DomibusPropertyMetadataDTO meta = getKnownPropertyMetadata(propertyName);
        if (meta == null) {
            throw new IllegalArgumentException("Property " + propertyName + " not found");
        }

        // TODO: handle multiple fsplugin domains in single-tenancy mode
        if (!domibusConfigurationExtService.isMultiTenantAware()) {
            domain = null;
        }

        final String propertyKey;
        if (domain == null) {
            propertyKey = PROPERTY_PREFIX + propertyName;
        } else {
            propertyKey = this.getDomainPropertyName(domain, propertyName);
        }
        return propertyKey;
    }

    @Override
    //TODO: reuse same code as in DomibusPropertyManager (EDELIVERY-4812)
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue) {
        setKnownPropertyValue(domainCode, propertyName, propertyValue, true);
    }

}
