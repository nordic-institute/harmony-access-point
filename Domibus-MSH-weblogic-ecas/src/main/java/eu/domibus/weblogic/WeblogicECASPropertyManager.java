package eu.domibus.weblogic;

import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.ext.domain.Module;
import eu.domibus.weblogic.property.WeblogicCommonPropertyManager;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static eu.domibus.weblogic.security.ECASUserDetailsService.*;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Property manager for the Weblogic ECAS specific properties.
 */
@Service//("serverPropertyManager")
public class WeblogicECASPropertyManager extends WeblogicCommonPropertyManager {
    private Map<String, DomibusPropertyMetadata> allProperties;

    private Map<String, DomibusPropertyMetadata> myProperties = Arrays.stream(new String[]{
            ECAS_DOMIBUS_LDAP_GROUP_PREFIX_KEY,
            ECAS_DOMIBUS_USER_ROLE_MAPPINGS_KEY,
            ECAS_DOMIBUS_DOMAIN_MAPPINGS_KEY
    })
            .map(name -> DomibusPropertyMetadata.getReadOnlyGlobalProperty(name, Module.WEBLOGIC_ECAS))
            .collect(Collectors.toMap(x -> x.getName(), x -> x));


    public WeblogicECASPropertyManager() {
        super(Module.WEBLOGIC_ECAS);
    }

    @Override
    public synchronized Map<String, DomibusPropertyMetadata> getKnownProperties() {
        if (allProperties == null) {
            allProperties = new HashMap<>();
            allProperties.putAll(super.getKnownProperties());
            allProperties.putAll(myProperties);
        }
        return allProperties;
    }
}
