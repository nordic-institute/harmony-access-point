package eu.domibus.plugin.fs.property;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO.Type;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO.Usage;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomibusPropertyMetadataManagerExt;
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

    //protected static final String DOMAIN_PREFIX = "fsplugin.domains.";

    protected static final String LOCATION = PROPERTY_PREFIX + "messages.location";

    protected static final String SENT_ACTION = PROPERTY_PREFIX+ "messages.sent.action";

    public static final String SENT_PURGE_WORKER_CRONEXPRESSION = PROPERTY_PREFIX+ "messages.sent.purge.worker.cronExpression";

    protected static final String SENT_PURGE_EXPIRED = PROPERTY_PREFIX+ "messages.sent.purge.expired";

    protected static final String FAILED_ACTION = PROPERTY_PREFIX+"messages.failed.action";

    public static final String FAILED_PURGE_WORKER_CRONEXPRESSION = PROPERTY_PREFIX + "messages.failed.purge.worker.cronExpression";

    protected static final String FAILED_PURGE_EXPIRED = PROPERTY_PREFIX + "messages.failed.purge.expired";

    protected static final String RECEIVED_PURGE_EXPIRED = PROPERTY_PREFIX + "messages.received.purge.expired";

    public static final String OUT_QUEUE_CONCURRENCY = PROPERTY_PREFIX +"send.queue.concurrency";

    protected static final String SEND_DELAY = PROPERTY_PREFIX + "messages.send.delay";

    public static final String SEND_WORKER_INTERVAL = PROPERTY_PREFIX + "messages.send.worker.repeatInterval";

    public static final String RECEIVED_PURGE_WORKER_CRONEXPRESSION = PROPERTY_PREFIX + "messages.received.purge.worker.cronExpression";

    public static final String LOCKS_PURGE_WORKER_CRONEXPRESSION = PROPERTY_PREFIX + "messages.locks.purge.worker.cronExpression";

    protected static final String LOCKS_PURGE_EXPIRED = PROPERTY_PREFIX + "messages.locks.purge.expired";

    protected static final String USER = PROPERTY_PREFIX + "messages.user";

    protected static final String PAYLOAD_ID = PROPERTY_PREFIX + "messages.payload.id";

    // Sonar confuses this constant with an actual password
    @SuppressWarnings("squid:S2068")
    protected static final String PASSWORD = PROPERTY_PREFIX + "messages.password";

    public static final String MESSAGE_NOTIFICATIONS = PROPERTY_PREFIX + "messages.notifications";

    protected static final String AUTHENTICATION_USER = PROPERTY_PREFIX + "authentication.user";

    // Sonar confuses this constant with an actual password
    @SuppressWarnings("squid:S2068")
    protected static final String AUTHENTICATION_PASSWORD = PROPERTY_PREFIX + "authentication.password";

    public static final String EXPRESSION = PROPERTY_PREFIX + "messages.expression";

    public static final String ORDER = PROPERTY_PREFIX + "order";

    protected static final String PAYLOAD_SCHEDULE_THRESHOLD = PROPERTY_PREFIX + "messages.payload.schedule.threshold";

    protected static final String PASSWORD_ENCRYPTION_ACTIVE = PROPERTY_PREFIX + "password.encryption.active"; //NOSONAR

    public static final String FSPLUGIN_PASSWORD_ENCRYPTION_PROPERTIES = PROPERTY_PREFIX + "password.encryption.properties"; //NOSONAR

    public static final String OUT_QUEUE = PROPERTY_PREFIX + "send.queue";

    protected static final String DOMAINS_LIST = PROPERTY_PREFIX + "domains.list";

    Map<String, DomibusPropertyMetadataDTO> knownProperties;

    public FSPluginPropertiesMetadataManagerImpl() {
        createMetadata();
    }

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

    protected void createMetadata() {
        knownProperties = Arrays.stream(new DomibusPropertyMetadataDTO[]{
                //non-writable properties:
                new DomibusPropertyMetadataDTO(MESSAGE_NOTIFICATIONS, Type.COMMA_SEPARATED_LIST, Module.FS_PLUGIN, false, Usage.GLOBAL, false, true, false, false),
                new DomibusPropertyMetadataDTO(PASSWORD_ENCRYPTION_ACTIVE, Type.BOOLEAN, Module.FS_PLUGIN, false, Usage.DOMAIN, false, true, false, false),
                new DomibusPropertyMetadataDTO(FSPLUGIN_PASSWORD_ENCRYPTION_PROPERTIES, Type.COMMA_SEPARATED_LIST, Module.FS_PLUGIN, false, Usage.DOMAIN, false, true, false, false),
                new DomibusPropertyMetadataDTO(OUT_QUEUE, Type.JNDI, Module.FS_PLUGIN, false, Usage.GLOBAL, true, true, false, false),
                new DomibusPropertyMetadataDTO(DOMAINS_LIST, Type.COMMA_SEPARATED_LIST, Module.FS_PLUGIN, false, Usage.GLOBAL, false, true, false, false),

                //writable properties
                new DomibusPropertyMetadataDTO(SEND_WORKER_INTERVAL, Type.NUMERIC, Module.FS_PLUGIN, Usage.DOMAIN, true),
                new DomibusPropertyMetadataDTO(SENT_PURGE_WORKER_CRONEXPRESSION, Type.CRON, Module.FS_PLUGIN, Usage.DOMAIN, true),
                new DomibusPropertyMetadataDTO(FAILED_PURGE_WORKER_CRONEXPRESSION, Type.CRON, Module.FS_PLUGIN, Usage.DOMAIN, true),
                new DomibusPropertyMetadataDTO(RECEIVED_PURGE_WORKER_CRONEXPRESSION, Type.CRON, Module.FS_PLUGIN, Usage.DOMAIN, true),
                new DomibusPropertyMetadataDTO(LOCKS_PURGE_WORKER_CRONEXPRESSION, Type.CRON, Module.FS_PLUGIN, Usage.DOMAIN, true),
                // without fallback
                new DomibusPropertyMetadataDTO(AUTHENTICATION_USER, Module.FS_PLUGIN, Usage.DOMAIN, false),
                new DomibusPropertyMetadataDTO(AUTHENTICATION_PASSWORD, Type.PASSWORD, Module.FS_PLUGIN, true, Usage.DOMAIN, false, true, true, false),
                new DomibusPropertyMetadataDTO(USER, Module.FS_PLUGIN, Usage.DOMAIN, false),
                new DomibusPropertyMetadataDTO(PASSWORD, Type.PASSWORD, Module.FS_PLUGIN, true, Usage.DOMAIN, false, true, true, false),
                // with fallback - domain like in ST and full domain in MT
                new DomibusPropertyMetadataDTO(LOCATION, Type.URI, Module.FS_PLUGIN, Usage.DOMAIN, true),
                new DomibusPropertyMetadataDTO(ORDER, Type.NUMERIC, Module.FS_PLUGIN, Usage.DOMAIN, true),
                new DomibusPropertyMetadataDTO(EXPRESSION, Type.REGEXP, Module.FS_PLUGIN, Usage.DOMAIN, true),
                new DomibusPropertyMetadataDTO(SEND_DELAY, Type.NUMERIC, Module.FS_PLUGIN, Usage.DOMAIN, true),
                new DomibusPropertyMetadataDTO(PAYLOAD_SCHEDULE_THRESHOLD, Type.NUMERIC, Module.FS_PLUGIN, Usage.DOMAIN, true),
                new DomibusPropertyMetadataDTO(SENT_ACTION, Module.FS_PLUGIN, Usage.DOMAIN, true),
                new DomibusPropertyMetadataDTO(FAILED_ACTION, Module.FS_PLUGIN, Usage.DOMAIN, true),
                new DomibusPropertyMetadataDTO(SENT_PURGE_EXPIRED, Type.NUMERIC, Module.FS_PLUGIN, Usage.DOMAIN, true),
                new DomibusPropertyMetadataDTO(FAILED_PURGE_EXPIRED, Type.NUMERIC, Module.FS_PLUGIN, Usage.DOMAIN, true),
                new DomibusPropertyMetadataDTO(RECEIVED_PURGE_EXPIRED, Type.NUMERIC, Module.FS_PLUGIN, Usage.DOMAIN, true),
                new DomibusPropertyMetadataDTO(LOCKS_PURGE_EXPIRED, Type.NUMERIC, Module.FS_PLUGIN, Usage.DOMAIN, true),
                new DomibusPropertyMetadataDTO(PAYLOAD_ID, Type.URI, Module.FS_PLUGIN, Usage.DOMAIN, true),
                new DomibusPropertyMetadataDTO(OUT_QUEUE_CONCURRENCY, Type.CONCURRENCY, Module.FS_PLUGIN, Usage.DOMAIN, true),
        })
                .collect(Collectors.toMap(x -> x.getName(), x -> x));
    }
}
