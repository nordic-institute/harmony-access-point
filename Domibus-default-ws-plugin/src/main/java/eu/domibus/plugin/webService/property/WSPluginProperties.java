package eu.domibus.plugin.webService.property;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.webService.impl.BackendWebServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import sun.rmi.runtime.Log;

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

    @Override
    public boolean hasKnownProperty(String name) {
        return StringUtils.equalsAnyIgnoreCase(name, SCHEMA_VALIDATION_ENABLED_PROPERTY, MTOM_ENABLED_PROPERTY);
    }

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        return Arrays.stream(new DomibusPropertyMetadataDTO[]{
                new DomibusPropertyMetadataDTO(SCHEMA_VALIDATION_ENABLED_PROPERTY, Module.WS_PLUGIN, false),
                new DomibusPropertyMetadataDTO(MTOM_ENABLED_PROPERTY, Module.WS_PLUGIN, false),
        }).collect(Collectors.toMap(x -> x.getName(), x -> x));
    }

    @Override
    public String getKnownPropertyValue(String domainCode, String propertyName) {
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
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue) {
        setKnownPropertyValue(domainCode, propertyName, propertyValue, true);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast) {
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

    private Endpoint getEndpoint() {
        return applicationContext.getBean("backendInterfaceEndpoint", Endpoint.class);
    }

    private Boolean isMtomEnabled() {
        Endpoint ep = getEndpoint();
        return ((SOAPBinding) ep.getBinding()).isMTOMEnabled();
    }

    private void setMtomEnabled(Boolean flag) {
        Endpoint ep = getEndpoint();
        ((SOAPBinding) ep.getBinding()).setMTOMEnabled(flag);
    }

    private Boolean isSchemaValidationEnabled() {
        Endpoint ep = getEndpoint();
        return "true".equals(ep.getProperties().get("schema-validation-enabled"));
    }

    private void setSchemaValidationEnabled(Boolean flag) {
        Endpoint ep = getEndpoint();
        ep.getProperties().put("schema-validation-enabled", flag.toString());
    }

}