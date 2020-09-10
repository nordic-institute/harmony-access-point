package eu.domibus.plugin.jms.property;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO.Type;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO.Usage;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomibusPropertyExtServiceDelegateAbstract;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static eu.domibus.plugin.jms.JMSMessageConstants.*;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Property manager for the JmsPlugin properties.
 */
@Service
public class JmsPluginPropertyManager extends DomibusPropertyExtServiceDelegateAbstract {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(JmsPluginPropertyManager.class);

    private List<DomibusPropertyMetadataDTO> readOnlyGlobalProperties = Arrays.asList(
            new DomibusPropertyMetadataDTO(CONNECTION_FACTORY, Type.JNDI, Module.JMS_PLUGIN, false, Usage.GLOBAL, true, false, false, false),
            new DomibusPropertyMetadataDTO(QUEUE_NOTIFICATION, Type.JNDI, Module.JMS_PLUGIN, false, Usage.GLOBAL, false, false, false, false),
            new DomibusPropertyMetadataDTO(QUEUE_IN, Type.JNDI, Module.JMS_PLUGIN, false, Usage.GLOBAL, false, false, false, false),
            new DomibusPropertyMetadataDTO(QUEUE_IN_CONCURRENCY, Type.CONCURRENCY, Module.JMS_PLUGIN, false, Usage.GLOBAL, false, false, false, false)
    );

    private List<DomibusPropertyMetadataDTO> readOnlyDomainProperties = Arrays.stream(new String[]{
            JMSPLUGIN_QUEUE_OUT,
            JMSPLUGIN_QUEUE_REPLY,
            JMSPLUGIN_QUEUE_CONSUMER_NOTIFICATION_ERROR,
            JMSPLUGIN_QUEUE_PRODUCER_NOTIFICATION_ERROR,
            JMS_PLUGIN_PROPERTY_PREFIX + "." + P1_IN_BODY
    })
            .map(name -> new DomibusPropertyMetadataDTO(name, Module.JMS_PLUGIN, false, Usage.DOMAIN, true, false, false, false))
            .collect(Collectors.toList());

    private List<DomibusPropertyMetadataDTO> readOnlyComposableDomainProperties = Arrays.stream(new String[]{
            JMSPLUGIN_QUEUE_OUT_ROUTING,
            JMSPLUGIN_QUEUE_REPLY_ROUTING,
            JMSPLUGIN_QUEUE_CONSUMER_NOTIFICATION_ERROR_ROUTING,
            JMSPLUGIN_QUEUE_PRODUCER_NOTIFICATION_ERROR_ROUTING
    })
            .map(name -> new DomibusPropertyMetadataDTO(name, Module.JMS_PLUGIN, false, Usage.DOMAIN, false, false, false, true))
            .collect(Collectors.toList());


    private List<DomibusPropertyMetadataDTO> writableProperties = Arrays.asList(
            new DomibusPropertyMetadataDTO(JMS_PLUGIN_PROPERTY_PREFIX + "." + FROM_PARTY_ID, Module.JMS_PLUGIN, Usage.DOMAIN, true),
            new DomibusPropertyMetadataDTO(JMS_PLUGIN_PROPERTY_PREFIX + "." + FROM_PARTY_TYPE, Type.URI, Module.JMS_PLUGIN, Usage.DOMAIN, true),
            new DomibusPropertyMetadataDTO(JMS_PLUGIN_PROPERTY_PREFIX + "." + FROM_ROLE, Type.URI, Module.JMS_PLUGIN, Usage.DOMAIN, true),
            new DomibusPropertyMetadataDTO(JMS_PLUGIN_PROPERTY_PREFIX + "." + TO_PARTY_ID, Module.JMS_PLUGIN, Usage.DOMAIN, true),
            new DomibusPropertyMetadataDTO(JMS_PLUGIN_PROPERTY_PREFIX + "." + TO_PARTY_TYPE, Type.URI, Module.JMS_PLUGIN, Usage.DOMAIN, true),
            new DomibusPropertyMetadataDTO(JMS_PLUGIN_PROPERTY_PREFIX + "." + TO_ROLE, Type.URI, Module.JMS_PLUGIN, Usage.DOMAIN, true),
            new DomibusPropertyMetadataDTO(JMS_PLUGIN_PROPERTY_PREFIX + "." + AGREEMENT_REF, Module.JMS_PLUGIN, Usage.DOMAIN, true),
            new DomibusPropertyMetadataDTO(JMS_PLUGIN_PROPERTY_PREFIX + "." + SERVICE, Module.JMS_PLUGIN, Usage.DOMAIN, true),
            new DomibusPropertyMetadataDTO(JMS_PLUGIN_PROPERTY_PREFIX + "." + SERVICE_TYPE, Module.JMS_PLUGIN, Usage.DOMAIN, true),
            new DomibusPropertyMetadataDTO(JMS_PLUGIN_PROPERTY_PREFIX + "." + ACTION, Module.JMS_PLUGIN, Usage.DOMAIN, true),
            new DomibusPropertyMetadataDTO(JMS_PLUGIN_PROPERTY_PREFIX + "." + PUT_ATTACHMENTS_IN_QUEUE, Type.BOOLEAN, Module.JMS_PLUGIN, Usage.DOMAIN, true)
    );

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        List<DomibusPropertyMetadataDTO> allProperties = new ArrayList<>();
        allProperties.addAll(readOnlyGlobalProperties);
        allProperties.addAll(readOnlyDomainProperties);
        allProperties.addAll(readOnlyComposableDomainProperties);
        allProperties.addAll(writableProperties);

        return allProperties.stream().collect(Collectors.toMap(x -> x.getName(), x -> x));
    }
}
