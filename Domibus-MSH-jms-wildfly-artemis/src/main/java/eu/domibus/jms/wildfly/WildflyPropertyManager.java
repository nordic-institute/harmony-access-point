package eu.domibus.jms.wildfly;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomibusPropertyExtServiceDelegateAbstract;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Property manager for the Wildfly Artemis specific properties.
 */
@Service
public class WildflyPropertyManager extends DomibusPropertyExtServiceDelegateAbstract {

    public static final String DOMIBUS_JMS_ACTIVEMQ_ARTEMIS_BROKER = "domibus.jms.activemq.artemis.broker";

    private Map<String, DomibusPropertyMetadataDTO> knownProperties = Arrays.stream(new String[]{
            DOMIBUS_JMS_ACTIVEMQ_ARTEMIS_BROKER
    })
            .map(name -> new DomibusPropertyMetadataDTO(name, Module.DSS, false, DomibusPropertyMetadataDTO.Usage.DOMAIN, true, true, false, true))
            .collect(Collectors.toMap(x -> x.getName(), x -> x));

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        return knownProperties;
    }

}
