package eu.domibus.plugin.jms.property;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomibusPropertyExtServiceDelegateAbstract;
import org.apache.commons.lang3.StringUtils;
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

    private List<DomibusPropertyMetadataDTO> readOnlyGlobalProperties = Arrays.stream(new String[]{
            QUEUE_NOTIFICATION,
            QUEUE_IN,
            QUEUE_IN_CONCURRENCY
    })
            .map(name -> new DomibusPropertyMetadataDTO(JMS_PLUGIN_PROPERTY_PREFIX + "." + name, Module.JMS_PLUGIN, false, DomibusPropertyMetadataDTO.Usage.GLOBAL, false, false, false, false))
            .collect(Collectors.toList());

    private List<DomibusPropertyMetadataDTO> readOnlyDomainProperties = Arrays.stream(new String[]{
            JMSPLUGIN_QUEUE_OUT,
            JMSPLUGIN_QUEUE_REPLY,
            JMSPLUGIN_QUEUE_CONSUMER_NOTIFICATION_ERROR,
            JMSPLUGIN_QUEUE_PRODUCER_NOTIFICATION_ERROR
    })
            .map(name -> new DomibusPropertyMetadataDTO(name, Module.JMS_PLUGIN, false, DomibusPropertyMetadataDTO.Usage.DOMAIN, true, false, false, false))
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
        allProperties.addAll(writableProperties);

        return allProperties.stream().collect(Collectors.toMap(x -> x.getName(), x -> x));
    }

}
