
package eu.domibus.plugin.fs.property;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.ext.services.DomibusPropertyExtServiceDelegateAbstract;
import eu.domibus.ext.services.PasswordEncryptionExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.*;
import static eu.domibus.plugin.fs.worker.FSSendMessagesService.DEFAULT_DOMAIN;

/**
 * File System Plugin Properties
 * <p>
 * All the plugin configurable properties must be accessed and handled through this component.
 *
 * @author FERNANDES Henrique
 * @author GONCALVES Bruno
 * @author Catalin Enache
 */
@Service
public class FSPluginProperties extends DomibusPropertyExtServiceDelegateAbstract {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSPluginProperties.class);

    public static final String PLUGIN_PROPERTIES_FILE_NAME = "fs-plugin.properties";

    @Autowired
    protected PasswordEncryptionExtService pluginPasswordEncryptionService;

    @Autowired
    protected DomibusConfigurationExtService domibusConfigurationExtService;

    @Autowired
    protected FSPluginPropertiesMetadataManagerImpl fsPluginPropertiesMetadataManager;

    @Autowired
    protected DomainContextExtService domainContextProvider;

    public static final String ACTION_DELETE = "delete";

    public static final String ACTION_ARCHIVE = "archive";

    /**
     * @param domain The domain property qualifier
     */
    public boolean getDomainEnabled(String domain) {
        String value = getDomainProperty(domain, DOMAIN_ENABLED);
        return BooleanUtils.toBoolean(value);
    }

    /**
     * @param domain The domain property qualifier
     * @return The location of the directory that the plugin will use to manage the messages to be sent and received
     */
    public String getLocation(String domain) {
        return getDomainProperty(domain, LOCATION);
    }

    /**
     * @param domain The domain property qualifier
     * @return The plugin action when message is sent successfully from C2 to C3 ('delete' or 'archive')
     */
    public String getSentAction(String domain) {
        return getDomainProperty(domain, SENT_ACTION);
    }

    /**
     * @param domain The domain property qualifier
     * @return The cron expression that defines the frequency of the sent messages purge job
     */
    public String getSentPurgeWorkerCronExpression(String domain) {
        return getDomainProperty(domain, SENT_PURGE_WORKER_CRONEXPRESSION);
    }

    /**
     * @param domain The domain property qualifier
     * @return The time interval (seconds) to purge sent messages
     */
    public Integer getSentPurgeExpired(String domain) {
        return getDomainIntegerProperty(domain, SENT_PURGE_EXPIRED);
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
     * @param domain The domain property qualifier
     * @return The cron expression that defines the frequency of the failed messages purge job
     */
    public String getFailedPurgeWorkerCronExpression(String domain) {
        return getDomainProperty(domain, FAILED_PURGE_WORKER_CRONEXPRESSION);
    }

    /**
     * @param domain The domain property qualifier
     * @return The cron expression that defines the frequency of the received messages purge job
     */
    public String getReceivedPurgeWorkerCronExpression(String domain) {
        return getDomainProperty(domain, RECEIVED_PURGE_WORKER_CRONEXPRESSION);
    }

    /**
     * @param domain The domain property qualifier
     * @return The cron expression that defines the frequency of the orphan lock files purge job
     */
    public String getLocksPurgeWorkerCronExpression(String domain) {
        return getDomainProperty(domain, LOCKS_PURGE_WORKER_CRONEXPRESSION);
    }

    /**
     * @param domain The domain property qualifier
     * @return The time interval (seconds) to purge failed messages
     */
    public Integer getFailedPurgeExpired(String domain) {
        return getDomainIntegerProperty(domain, FAILED_PURGE_EXPIRED);
    }

    /**
     * @param domain The domain property qualifier
     * @return The time interval (seconds) to purge received messages
     */
    public Integer getReceivedPurgeExpired(String domain) {
        return getDomainIntegerProperty(domain, RECEIVED_PURGE_EXPIRED);
    }

    /**
     * @param domain The domain property qualifier
     * @return The time interval (seconds) to purge orphan lock files
     */
    public Integer getLocksPurgeExpired(String domain) {
        return getDomainIntegerProperty(domain, LOCKS_PURGE_EXPIRED);
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
        return decryptPasswordProperty(domain, PASSWORD);
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
        return decryptPasswordProperty(domain, AUTHENTICATION_PASSWORD);
    }

    private String decryptPasswordProperty(String domain, String passwordPropertyToDecrypt) {
        return getDomainProperty(domain, passwordPropertyToDecrypt);
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
        return getDomainIntegerProperty(domain, SEND_DELAY);
    }

    /**
     * @param domain The domain property qualifier
     * @return send worker interval in milliseconds
     */
    public Integer getSendWorkerInterval(String domain) {
        return getDomainIntegerProperty(domain, SEND_WORKER_INTERVAL);
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
     * get the base (mapped to default) and other domains property
     *
     * @param domain
     * @param propertyName
     * @return
     */
    public String getDomainProperty(String domain, String propertyName) {
        return getKnownPropertyValue(domain, propertyName);
    }

    protected Integer getDomainIntegerProperty(String domain, String propertyName) {
        String value = getDomainProperty(domain, propertyName);
        LOG.trace("Domain [{}] property [{}]=[{}]", domain, propertyName, value);
        return NumberUtils.toInt(value, 0);
    }

    @Override
    public synchronized Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        return fsPluginPropertiesMetadataManager.getKnownProperties();
    }

    @Override
    protected String getPropertiesFileName() {
        return PLUGIN_PROPERTIES_FILE_NAME;
    }

}
