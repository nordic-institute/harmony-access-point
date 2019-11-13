package eu.domibus.weblogic;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomibusPropertyExtServiceDelegateAbstract;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.*;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Common property manager for the Weblogic servers/implementations specific properties.
 * Weblogic and ECAS managers derive from this
 */
public class WeblogicCommonPropertyManager extends DomibusPropertyExtServiceDelegateAbstract {

    private Map<String, DomibusPropertyMetadataDTO> knownProperties = Arrays.stream(new String[]{
            DOMIBUS_CLUSTER_COMMAND_CRON_EXPRESSION, //present just in domibus.properties but seems not to be used anywhere!!
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
