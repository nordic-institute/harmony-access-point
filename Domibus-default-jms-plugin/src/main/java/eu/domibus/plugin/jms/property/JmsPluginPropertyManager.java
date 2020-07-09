package eu.domibus.plugin.jms.property;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomibusPropertyExtServiceDelegateAbstract;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
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
            new DomibusPropertyMetadataDTO(JMS_PLUGIN_PROPERTY_PREFIX + "." + CONNECTION_FACTORY, Module.JMS_PLUGIN, false, DomibusPropertyMetadataDTO.Usage.GLOBAL, true, false, false, false),
            new DomibusPropertyMetadataDTO(JMS_PLUGIN_PROPERTY_PREFIX + "." + QUEUE_NOTIFICATION, Module.JMS_PLUGIN, false, DomibusPropertyMetadataDTO.Usage.GLOBAL, false, false, false, false),
            new DomibusPropertyMetadataDTO(JMS_PLUGIN_PROPERTY_PREFIX + "." + QUEUE_IN, Module.JMS_PLUGIN, false, DomibusPropertyMetadataDTO.Usage.GLOBAL, false, false, false, false),
            new DomibusPropertyMetadataDTO(JMS_PLUGIN_PROPERTY_PREFIX + "." + QUEUE_IN_CONCURRENCY, DomibusPropertyMetadataDTO.Type.CONCURRENCY, Module.JMS_PLUGIN, false, DomibusPropertyMetadataDTO.Usage.GLOBAL, false, false, false, false)
    );


    private List<DomibusPropertyMetadataDTO> readOnlyDomainProperties = Arrays.stream(new String[]{
            JMSPLUGIN_QUEUE_OUT,
            JMSPLUGIN_QUEUE_REPLY,
            JMSPLUGIN_QUEUE_CONSUMER_NOTIFICATION_ERROR,
            JMSPLUGIN_QUEUE_PRODUCER_NOTIFICATION_ERROR,
            JMS_PLUGIN_PROPERTY_PREFIX + "." + P1_IN_BODY
    })
            .map(name -> new DomibusPropertyMetadataDTO(name, Module.JMS_PLUGIN, false, DomibusPropertyMetadataDTO.Usage.DOMAIN, true, false, false, false))
            .collect(Collectors.toList());

    private List<DomibusPropertyMetadataDTO> readOnlyComposableDomainProperties = Arrays.stream(new String[]{
            JMSPLUGIN_QUEUE_OUT_ROUTING,
            JMSPLUGIN_QUEUE_REPLY_ROUTING,
            JMSPLUGIN_QUEUE_CONSUMER_NOTIFICATION_ERROR_ROUTING,
            JMSPLUGIN_QUEUE_PRODUCER_NOTIFICATION_ERROR_ROUTING
    })
            .map(name -> new DomibusPropertyMetadataDTO(name, Module.JMS_PLUGIN, false, DomibusPropertyMetadataDTO.Usage.DOMAIN, false, false, false, true))
            .collect(Collectors.toList());

    private List<DomibusPropertyMetadataDTO> writableProperties = Arrays.stream(new String[]{
            FROM_PARTY_ID, FROM_PARTY_TYPE, FROM_ROLE,
            TO_PARTY_ID, TO_PARTY_TYPE, TO_ROLE,
            AGREEMENT_REF, SERVICE, SERVICE_TYPE, ACTION,
            PUT_ATTACHMENTS_IN_QUEUE
    })
            .map(name -> new DomibusPropertyMetadataDTO(JMS_PLUGIN_PROPERTY_PREFIX + "." + name, Module.JMS_PLUGIN, DomibusPropertyMetadataDTO.Usage.DOMAIN, true))
            .collect(Collectors.toList());

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
