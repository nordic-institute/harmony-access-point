package eu.domibus.plugin.jms.property;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomibusPropertyExtServiceDelegateAbstract;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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

    @Override
    public String getKnownPropertyValue(String propertyName) {
        if (!hasKnownProperty(propertyName)) {
            throw new IllegalArgumentException("Unknown property: " + propertyName);
        }

        if (StringUtils.equalsIgnoreCase(propertyName, PUT_ATTACHMENTS_IN_QUEUE)) {
            String value = domibusPropertyExtService.getProperty(propertyName);
            if (StringUtils.isEmpty(value)) {
                value = "true";
            }
            return value;
        }

        return domibusPropertyExtService.getProperty(propertyName);
    }

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        String[] knownPropertyNames = new String[]{
                FROM_PARTY_ID, FROM_PARTY_TYPE, FROM_ROLE,
                TO_PARTY_ID, TO_PARTY_TYPE, TO_ROLE,
                AGREEMENT_REF, SERVICE, SERVICE_TYPE, ACTION,
                PUT_ATTACHMENTS_IN_QUEUE,
                "queue.out",
        };
        return Arrays.stream(knownPropertyNames)
                .map(name -> new DomibusPropertyMetadataDTO(JMS_PLUGIN_PROPERTY_PREFIX + "." + name, Module.JMS_PLUGIN, DomibusPropertyMetadataDTO.Usage.DOMAIN, true))
                .collect(Collectors.toMap(x -> x.getName(), x -> x));
    }

}
