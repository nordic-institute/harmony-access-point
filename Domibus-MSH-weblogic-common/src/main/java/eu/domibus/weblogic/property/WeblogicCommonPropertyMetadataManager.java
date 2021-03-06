package eu.domibus.weblogic.property;

import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyMetadata.Type;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Common property manager for the Weblogic servers/implementations specific properties.
 * Weblogic and ECAS managers derive from this
 */
public class WeblogicCommonPropertyMetadataManager implements DomibusPropertyMetadataManagerSPI {

    String module;

    public WeblogicCommonPropertyMetadataManager(String module) {
        this.module = module;
    }

    private Map<String, DomibusPropertyMetadata> knownProperties = Arrays.asList(
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_CLUSTER_COMMAND_CRON_EXPRESSION, Type.CRON, module), //present just in domibus.properties but seems not to be used anywhere!!
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_JMX_USER, module),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_JMX_PASSWORD, Type.PASSWORD, module),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(WEBLOGIC_MANAGEMENT_SERVER, module)
    ).stream().collect(Collectors.toMap(x -> x.getName(), x -> x));

    @Override
    public Map<String, DomibusPropertyMetadata> getKnownProperties() {
        return knownProperties;
    }

    @Override
    public boolean hasKnownProperty(String name) {
        return getKnownProperties().containsKey(name);
    }
}
