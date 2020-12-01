package eu.domibus.property;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO.Type;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO.Usage;
import eu.domibus.ext.services.DomibusPropertyExtServiceDelegateAbstract;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Property manager for integration test; handles locally some properties
 *
 * @author Ion Perpegel
 * @since 5.0
 */
@Component
public class ExternalModulePropertyManager extends DomibusPropertyExtServiceDelegateAbstract
        implements DomibusPropertyManagerExt {

    public static final String EXTERNAL_NOT_EXISTENT = "externalModule.notExistent";
    public static final String EXTERNAL_MODULE_EXISTENT_NOT_HANDLED = "externalModule.existent.notHandled";
    public static final String EXTERNAL_MODULE_EXISTENT_LOCALLY_HANDLED = "externalModule.existent.handled.locally";
    public static final String EXTERNAL_MODULE_EXISTENT_GLOBALLY_HANDLED = "externalModule.existent.handled.globally";

    private Map<String, DomibusPropertyMetadataDTO> knownProperties;

    private Map<String, String> knownPropertyValues = new HashMap<>();

    public ExternalModulePropertyManager() {
        List<DomibusPropertyMetadataDTO> allProperties = Arrays.asList(
                new DomibusPropertyMetadataDTO(EXTERNAL_MODULE_EXISTENT_NOT_HANDLED, Type.STRING, "ExternalModule", Usage.DOMAIN),
                new DomibusPropertyMetadataDTO(EXTERNAL_MODULE_EXISTENT_LOCALLY_HANDLED, Type.STRING, "ExternalModule", Usage.DOMAIN),
                new DomibusPropertyMetadataDTO(EXTERNAL_MODULE_EXISTENT_GLOBALLY_HANDLED, Type.STRING, "ExternalModule", Usage.DOMAIN)
        );

        knownProperties = allProperties.stream()
                .peek(prop -> prop.setStoredGlobally(false))
                .collect(Collectors.toMap(x -> x.getName(), x -> x));

        knownProperties.get(EXTERNAL_MODULE_EXISTENT_GLOBALLY_HANDLED).setStoredGlobally(true);

        knownPropertyValues.put(EXTERNAL_MODULE_EXISTENT_LOCALLY_HANDLED, EXTERNAL_MODULE_EXISTENT_LOCALLY_HANDLED + ".value");
    }

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        return knownProperties;
    }

    @Override
    protected String onGetLocalPropertyValue(String domainCode, String propertyName) {
        if (knownPropertyValues.containsKey(propertyName)) {
            return knownPropertyValues.get(propertyName);
        }
        return super.onGetLocalPropertyValue(domainCode, propertyName);
    }

    @Override
    protected void onSetLocalPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast) {
        if (knownPropertyValues.containsKey(propertyName)) {
            knownPropertyValues.put(propertyName, propertyValue);
            return;
        }
        super.onSetLocalPropertyValue(domainCode, propertyName, propertyValue, broadcast);
    }
}