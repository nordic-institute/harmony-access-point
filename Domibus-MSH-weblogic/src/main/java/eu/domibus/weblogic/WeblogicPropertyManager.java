package eu.domibus.weblogic;

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
 * Property manager for the Weblogic ECAS specific properties.
 */
@Service
public class WeblogicPropertyManager extends DomibusPropertyExtServiceDelegateAbstract {

    private Map<String, DomibusPropertyMetadataDTO> knownProperties = Arrays.stream(new String[]{
            DOMIBUS_CLUSTER_COMMAND_CRON_EXPRESSION,
            DOMIBUS_JMX_USER,
            DOMIBUS_JMX_PASSWORD,
            DOMIBUS_SECURITY_EXT_AUTH_PROVIDER_ENABLED
    })
            .map(name -> DomibusPropertyMetadataDTO.getReadOnlyGlobalProperty(name, Module.WEBLOGIC_ECAS))
            .collect(Collectors.toMap(x -> x.getName(), x -> x));

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        return knownProperties;
    }

}
