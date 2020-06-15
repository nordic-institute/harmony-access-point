package eu.domibus.plugin.fs.property;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.ext.services.DomibusPropertyMetadataManagerExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Property manager for the Default FS plugin properties.
 */
@Component
public class FSPluginPropertiesMetadataManagerImpl implements DomibusPropertyMetadataManagerExt {

    public static final String PROPERTY_PREFIX = "fsplugin.";

    protected static final String DOMAIN_PREFIX = "fsplugin.domains.";

    protected static final String LOCATION = "messages.location";

    protected static final String SENT_ACTION = "messages.sent.action";

    public static final String SENT_PURGE_WORKER_CRONEXPRESSION = "messages.sent.purge.worker.cronExpression";

    protected static final String SENT_PURGE_EXPIRED = "messages.sent.purge.expired";

    protected static final String FAILED_ACTION = "messages.failed.action";

    public static final String FAILED_PURGE_WORKER_CRONEXPRESSION = "messages.failed.purge.worker.cronExpression";

    protected static final String FAILED_PURGE_EXPIRED = "messages.failed.purge.expired";

    protected static final String RECEIVED_PURGE_EXPIRED = "messages.received.purge.expired";

    public static final String OUT_QUEUE_CONCURRENCY = "send.queue.concurrency";

    protected static final String SEND_DELAY = "messages.send.delay";

    public static final String SEND_WORKER_INTERVAL = "messages.send.worker.repeatInterval";

    public static final String RECEIVED_PURGE_WORKER_CRONEXPRESSION = "messages.received.purge.worker.cronExpression";

    protected static final String USER = "messages.user";

    protected static final String PAYLOAD_ID = "messages.payload.id";

    // Sonar confuses this constant with an actual password
    @SuppressWarnings("squid:S2068")
    protected static final String PASSWORD = "messages.password";

    protected static final String AUTHENTICATION_USER = "authentication.user";

    // Sonar confuses this constant with an actual password
    @SuppressWarnings("squid:S2068")
    protected static final String AUTHENTICATION_PASSWORD = "authentication.password";

    public static final String EXPRESSION = "messages.expression";

    public static final String ORDER = "order";

    protected static final String PAYLOAD_SCHEDULE_THRESHOLD = "messages.payload.schedule.threshold";

    protected static final String PASSWORD_ENCRYPTION_ACTIVE = "password.encryption.active"; //NOSONAR

    public static final String FSPLUGIN_PASSWORD_ENCRYPTION_PROPERTIES = "fsplugin.password.encryption.properties"; //NOSONAR

    protected static final String OUT_QUEUE = "send.queue";

    @Autowired
    protected DomibusConfigurationExtService domibusConfigurationExtService;

    Map<String, DomibusPropertyMetadataDTO> knownProperties = Arrays.stream(new DomibusPropertyMetadataDTO[]{
            //non-writable properties:
            new DomibusPropertyMetadataDTO(PASSWORD_ENCRYPTION_ACTIVE, DomibusPropertyMetadataDTO.Type.BOOLEAN, Module.FS_PLUGIN, false, DomibusPropertyMetadataDTO.Usage.DOMAIN, false, true, false, false),
            new DomibusPropertyMetadataDTO(FSPLUGIN_PASSWORD_ENCRYPTION_PROPERTIES, Module.FS_PLUGIN, false, DomibusPropertyMetadataDTO.Usage.DOMAIN, false, true, false, false),
            new DomibusPropertyMetadataDTO(OUT_QUEUE, Module.FS_PLUGIN, false, DomibusPropertyMetadataDTO.Usage.GLOBAL, false, true, false, false),

            //writable properties
            new DomibusPropertyMetadataDTO(SEND_WORKER_INTERVAL, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.FS_PLUGIN, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
            new DomibusPropertyMetadataDTO(SENT_PURGE_WORKER_CRONEXPRESSION, DomibusPropertyMetadataDTO.Type.CRON, Module.FS_PLUGIN, DomibusPropertyMetadataDTO.Usage.GLOBAL),
            new DomibusPropertyMetadataDTO(FAILED_PURGE_WORKER_CRONEXPRESSION, DomibusPropertyMetadataDTO.Type.CRON, Module.FS_PLUGIN, DomibusPropertyMetadataDTO.Usage.GLOBAL),
            new DomibusPropertyMetadataDTO(RECEIVED_PURGE_WORKER_CRONEXPRESSION, DomibusPropertyMetadataDTO.Type.CRON, Module.FS_PLUGIN, DomibusPropertyMetadataDTO.Usage.GLOBAL),
            // without fallback from the default domain :
            new DomibusPropertyMetadataDTO(AUTHENTICATION_USER, Module.FS_PLUGIN, DomibusPropertyMetadataDTO.Usage.DOMAIN, false),
            new DomibusPropertyMetadataDTO(AUTHENTICATION_PASSWORD, Module.FS_PLUGIN, true, DomibusPropertyMetadataDTO.Usage.DOMAIN, false, true, true, false),
            new DomibusPropertyMetadataDTO(USER, Module.FS_PLUGIN, DomibusPropertyMetadataDTO.Usage.DOMAIN, false),
            new DomibusPropertyMetadataDTO(PASSWORD, Module.FS_PLUGIN, true, DomibusPropertyMetadataDTO.Usage.DOMAIN, false, true, true, false),
            // with fallback from the default domain:
            new DomibusPropertyMetadataDTO(LOCATION, Module.FS_PLUGIN, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
            new DomibusPropertyMetadataDTO(ORDER, Module.FS_PLUGIN, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
            new DomibusPropertyMetadataDTO(EXPRESSION, Module.FS_PLUGIN, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
            new DomibusPropertyMetadataDTO(SEND_DELAY, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.FS_PLUGIN, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
            new DomibusPropertyMetadataDTO(PAYLOAD_SCHEDULE_THRESHOLD, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.FS_PLUGIN, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
            new DomibusPropertyMetadataDTO(SENT_ACTION, Module.FS_PLUGIN, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
            new DomibusPropertyMetadataDTO(FAILED_ACTION, Module.FS_PLUGIN, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
            new DomibusPropertyMetadataDTO(SENT_PURGE_EXPIRED, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.FS_PLUGIN, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
            new DomibusPropertyMetadataDTO(FAILED_PURGE_EXPIRED, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.FS_PLUGIN, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
            new DomibusPropertyMetadataDTO(RECEIVED_PURGE_EXPIRED, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.FS_PLUGIN, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
            new DomibusPropertyMetadataDTO(PAYLOAD_ID, Module.FS_PLUGIN, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
            new DomibusPropertyMetadataDTO(OUT_QUEUE_CONCURRENCY, DomibusPropertyMetadataDTO.Type.CONCURRENCY, Module.FS_PLUGIN, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
    }).peek(el -> el.setStoredGlobally(false)).collect(Collectors.toMap(x -> x.getName(), x -> x));

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        return knownProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasKnownProperty(String name) {
        return this.getKnownProperties().containsKey(name);
    }

}
