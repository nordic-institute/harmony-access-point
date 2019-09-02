package eu.domibus.plugin.fs.property;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.ext.services.DomibusPropertyMetadataManagerExt;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class FSPluginPropertiesMetadataManagerImpl implements DomibusPropertyMetadataManagerExt {

    protected static final String DOT = ".";

    protected static final String PROPERTY_PREFIX = "fsplugin.";

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

    protected static final String PASSWORD_ENCRYPTION_ACTIVE = "password.encryption.active";

    @Autowired
    protected DomibusConfigurationExtService domibusConfigurationExtService;

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
                new DomibusPropertyMetadataDTO(EXPRESSION, Module.FS_PLUGIN, true, true),
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
        return this.getKnownProperties().containsKey(name);
    }

}
