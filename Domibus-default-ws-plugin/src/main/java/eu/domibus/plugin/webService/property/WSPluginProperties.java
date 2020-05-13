package eu.domibus.plugin.webService.property;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.xml.ws.Endpoint;
import javax.xml.ws.soap.SOAPBinding;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class WSPluginProperties implements DomibusPropertyManagerExt {

    private static final String SCHEMA_VALIDATION_ENABLED_PROPERTY = "wsplugin.schema.validation.enabled";

    private static final String MTOM_ENABLED_PROPERTY = "wsplugin.mtom.enabled";

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginProperties.class);

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    Endpoint backendInterfaceEndpoint;

    private Map<String, DomibusPropertyMetadataDTO> knownProperties = Arrays.stream(new DomibusPropertyMetadataDTO[]{
            new DomibusPropertyMetadataDTO(SCHEMA_VALIDATION_ENABLED_PROPERTY, DomibusPropertyMetadataDTO.Type.BOOLEAN, Module.WS_PLUGIN, DomibusPropertyMetadataDTO.Usage.GLOBAL),
            new DomibusPropertyMetadataDTO(MTOM_ENABLED_PROPERTY, DomibusPropertyMetadataDTO.Type.BOOLEAN, Module.WS_PLUGIN, DomibusPropertyMetadataDTO.Usage.GLOBAL),
    }).collect(Collectors.toMap(x -> x.getName(), x -> x));

    @Override
    public boolean hasKnownProperty(String name) {
        return StringUtils.equalsAnyIgnoreCase(name, SCHEMA_VALIDATION_ENABLED_PROPERTY, MTOM_ENABLED_PROPERTY);
    }

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        return knownProperties;
    }

    @Override
    public String getKnownPropertyValue(String propertyName) {
        switch (propertyName) {
            case SCHEMA_VALIDATION_ENABLED_PROPERTY:
                return this.isSchemaValidationEnabled().toString();
            case MTOM_ENABLED_PROPERTY:
                return this.isMtomEnabled().toString();
            default:
                LOG.debug("Property [{}] not found in known property list", propertyName);
                return null;
        }
    }

    @Override
    public void setKnownPropertyValue(String propertyName, String propertyValue) {
        Boolean value = Boolean.valueOf(propertyValue);
        switch (propertyName) {
            case SCHEMA_VALIDATION_ENABLED_PROPERTY:
                this.setSchemaValidationEnabled(value);
                break;
            case MTOM_ENABLED_PROPERTY:
                this.setMtomEnabled(value);
                break;
            default:
                LOG.debug("Property [{}] cannot be set because it is not found", propertyName);
        }
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast) {
        setKnownPropertyValue(propertyName, propertyValue);
    }

    @Override
    public String getKnownPropertyValue(String domainCode, String propertyName) {
        return getKnownPropertyValue(propertyName);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue) {
        setKnownPropertyValue(domainCode, propertyName, propertyValue, true);
    }

    private Boolean isMtomEnabled() {
        return ((SOAPBinding) backendInterfaceEndpoint.getBinding()).isMTOMEnabled();
    }

    private void setMtomEnabled(Boolean flag) {
        ((SOAPBinding) backendInterfaceEndpoint.getBinding()).setMTOMEnabled(flag);
    }

    private Boolean isSchemaValidationEnabled() {
        return "true".equals(backendInterfaceEndpoint.getProperties().get("schema-validation-enabled"));
    }

    private void setSchemaValidationEnabled(Boolean flag) {
        backendInterfaceEndpoint.getProperties().put("schema-validation-enabled", flag.toString());
    }

}