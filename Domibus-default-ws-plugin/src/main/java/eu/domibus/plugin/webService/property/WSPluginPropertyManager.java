package eu.domibus.plugin.webService.property;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO.Type;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO.Usage;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomibusPropertyExtServiceDelegateAbstract;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Property manager for ws plugin, now forwards all calls to domibus property provider as all his properties are managed in core
 *
 * @author Ion Perpegel
 * @since 4.1
 */
@Component
public class WSPluginPropertyManager extends DomibusPropertyExtServiceDelegateAbstract
        implements DomibusPropertyManagerExt {

    public static final String SCHEMA_VALIDATION_ENABLED_PROPERTY = "wsplugin.schema.validation.enabled";
    public static final String MTOM_ENABLED_PROPERTY = "wsplugin.mtom.enabled";
    public static final String PROP_LIST_PENDING_MESSAGES_MAXCOUNT = "wsplugin.messages.pending.list.max";
    public static final String MESSAGE_NOTIFICATIONS = "wsplugin.messages.notifications";

    public static final String RETRY_ENABLED = "wsplugin.retry.enabled";
    public static final String DISPATCHER_CONNECTION_TIMEOUT = "wsplugin.dispatcher.connectionTimeout";
    public static final String DISPATCHER_RECEIVE_TIMEOUT = "wsplugin.dispatcher.receiveTimeout";
    public static final String DISPATCHER_ALLOW_CHUNKING = "wsplugin.dispatcher.allowChunking";
    public static final String DISPATCHER_CHUNKING_THRESHOLD = "wsplugin.dispatcher.chunkingThreshold";
    public static final String DISPATCHER_CONNECTION_KEEP_ALIVE = "wsplugin.dispatcher.connection.keepAlive";
    public static final String DISPATCHER_CRON_EXPRESSION = "wsplugin.dispatcher.worker.cronExpression";
    public static final String DISPATCHER_SEND_QUEUE_NAME = "wsplugin.send.queue";
    public static final String DISPATCHER_SEND_QUEUE_CONCURRENCY = "wsplugin.send.queue.concurrency";

    private final Map<String, DomibusPropertyMetadataDTO> knownProperties;

    public WSPluginPropertyManager() {
        List<DomibusPropertyMetadataDTO> allProperties = Arrays.asList(
                new DomibusPropertyMetadataDTO(SCHEMA_VALIDATION_ENABLED_PROPERTY, Type.BOOLEAN, Module.WS_PLUGIN, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(MTOM_ENABLED_PROPERTY, Type.BOOLEAN, Module.WS_PLUGIN, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(PROP_LIST_PENDING_MESSAGES_MAXCOUNT, Type.NUMERIC, Module.WS_PLUGIN, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(MESSAGE_NOTIFICATIONS, Type.COMMA_SEPARATED_LIST, Module.WS_PLUGIN, Usage.GLOBAL),

                new DomibusPropertyMetadataDTO(RETRY_ENABLED, Type.BOOLEAN, Module.WS_PLUGIN, Usage.GLOBAL),

                new DomibusPropertyMetadataDTO(DISPATCHER_CONNECTION_TIMEOUT, Type.NUMERIC, Module.WS_PLUGIN, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DISPATCHER_RECEIVE_TIMEOUT, Type.NUMERIC, Module.WS_PLUGIN, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DISPATCHER_ALLOW_CHUNKING, Type.BOOLEAN, Module.WS_PLUGIN, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DISPATCHER_CHUNKING_THRESHOLD, Type.NUMERIC, Module.WS_PLUGIN, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DISPATCHER_CONNECTION_KEEP_ALIVE, Type.BOOLEAN, Module.WS_PLUGIN, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DISPATCHER_CRON_EXPRESSION, Type.STRING, Module.WS_PLUGIN, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DISPATCHER_SEND_QUEUE_NAME, Type.STRING, Module.WS_PLUGIN, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(DISPATCHER_SEND_QUEUE_CONCURRENCY, Type.STRING, Module.WS_PLUGIN, Usage.DOMAIN)
        );
        knownProperties = allProperties.stream().collect(toMap(DomibusPropertyMetadataDTO::getName, identity()));
    }

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        return knownProperties;
    }
}