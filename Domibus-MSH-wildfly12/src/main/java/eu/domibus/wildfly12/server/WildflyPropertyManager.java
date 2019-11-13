package eu.domibus.wildfly12.server;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomibusPropertyExtServiceDelegateAbstract;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static eu.domibus.jms.wildfly.InternalJMSManagerWildFlyArtemis.JMS_BROKER_PROPERTY;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Property manager for the Wildfly Artemis specific properties.
 */
@Service
public class WildflyPropertyManager extends DomibusPropertyExtServiceDelegateAbstract {

    private Map<String, DomibusPropertyMetadataDTO> knownProperties = Arrays.stream(new String[]{
            JMS_BROKER_PROPERTY
    })
            .map(name -> new DomibusPropertyMetadataDTO(name, Module.WILDFLY_ARTEMIS, false, DomibusPropertyMetadataDTO.Usage.GLOBAL, false, true, false, false))
            .collect(Collectors.toMap(x -> x.getName(), x -> x));

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        return knownProperties;
    }

}
