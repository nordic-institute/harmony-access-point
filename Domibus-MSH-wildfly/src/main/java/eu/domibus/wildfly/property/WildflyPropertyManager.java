package eu.domibus.wildfly.property;

import eu.domibus.api.property.DomibusPropertyManager;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.ext.domain.Module;
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
@Service//("serverPropertyManager")
public class WildflyPropertyManager implements DomibusPropertyManager {

    private Map<String, DomibusPropertyMetadata> knownProperties = Arrays.stream(new String[]{
            ACTIVE_MQ_ARTEMIS_BROKER
    })
            .map(name -> DomibusPropertyMetadata.getReadOnlyGlobalProperty(name, Module.WILDFLY_ARTEMIS))
            .collect(Collectors.toMap(x -> x.getName(), x -> x));

    @Override
    public Map<String, DomibusPropertyMetadata> getKnownProperties() {
        return knownProperties;
    }

    @Override
    public boolean hasKnownProperty(String name) {
        return getKnownProperties().containsKey(name);
    }
}
