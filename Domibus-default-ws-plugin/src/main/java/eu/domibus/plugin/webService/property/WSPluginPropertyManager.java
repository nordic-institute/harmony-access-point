package eu.domibus.plugin.webService.property;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomibusPropertyExtServiceDelegateAbstract;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class WSPluginPropertyManager extends DomibusPropertyExtServiceDelegateAbstract
        implements DomibusPropertyManagerExt {

    public static final String SCHEMA_VALIDATION_ENABLED_PROPERTY = "wsplugin.schema.validation.enabled";

    public static final String MTOM_ENABLED_PROPERTY = "wsplugin.mtom.enabled";

    public static final String PROP_LIST_PENDING_MESSAGES_MAXCOUNT = "wsplugin.messages.pending.list.max";

//    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginPropertyManager.class);
//    @Autowired
//    Endpoint backendInterfaceEndpoint;

//    @Autowired
//    protected DomainContextExtService domainContextProvider;

//    @Autowired
//    @Lazy
//    PluginPropertyChangeNotifier pluginPropertyChangeNotifier;

    private Map<String, DomibusPropertyMetadataDTO> knownProperties;

    public WSPluginPropertyManager() {
        List<DomibusPropertyMetadataDTO> allProperties = Arrays.asList(
                new DomibusPropertyMetadataDTO(SCHEMA_VALIDATION_ENABLED_PROPERTY, DomibusPropertyMetadataDTO.Type.BOOLEAN, Module.WS_PLUGIN, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(MTOM_ENABLED_PROPERTY, DomibusPropertyMetadataDTO.Type.BOOLEAN, Module.WS_PLUGIN, DomibusPropertyMetadataDTO.Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(PROP_LIST_PENDING_MESSAGES_MAXCOUNT, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.WS_PLUGIN, DomibusPropertyMetadataDTO.Usage.GLOBAL)
        );

//        List<DomibusPropertyMetadataDTO> knownStoredLocallyProperties = Arrays.stream(new String[]{
//                SCHEMA_VALIDATION_ENABLED_PROPERTY,
//                MTOM_ENABLED_PROPERTY,
//        })
//                .map(name -> new DomibusPropertyMetadataDTO(name, DomibusPropertyMetadataDTO.Type.BOOLEAN, Module.WS_PLUGIN, DomibusPropertyMetadataDTO.Usage.GLOBAL))
//                .peek(el -> el.setStoredGlobally(false))
//                .collect(Collectors.toList());
//
//        List<DomibusPropertyMetadataDTO> knownStoredGloballyProperties = Arrays.stream(new String[]{
//                PROP_LIST_PENDING_MESSAGES_MAXCOUNT
//        })
//                .map(name -> new DomibusPropertyMetadataDTO(name, DomibusPropertyMetadataDTO.Type.NUMERIC, Module.WS_PLUGIN, DomibusPropertyMetadataDTO.Usage.GLOBAL))
//                .collect(Collectors.toList());
//
//        List<DomibusPropertyMetadataDTO> allProperties = new ArrayList<>();
//        allProperties.addAll(knownStoredLocallyProperties);
//        allProperties.addAll(knownStoredGloballyProperties);

        knownProperties = allProperties.stream().collect(Collectors.toMap(x -> x.getName(), x -> x));
    }

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        return knownProperties;
    }

//    @Override
//    public boolean hasKnownProperty(String propertyName) {
//        return knownProperties.containsValue(propertyName);
//    }

//    @Override
//    public String getKnownPropertyValue(String propertyName) {
//        if (getKnownProperties().get(propertyName).isStoredGlobally()) {
//            return super.getKnownPropertyValue(propertyName);
//        }
//        return getOwnPropertyValue(propertyName);
//    }

//    protected String getOwnPropertyValue(String propertyName) {
//        switch (propertyName) {
//            case SCHEMA_VALIDATION_ENABLED_PROPERTY:
//                return this.isSchemaValidationEnabled().toString();
//            case MTOM_ENABLED_PROPERTY:
//                return this.isMtomEnabled().toString();
//            default:
//                LOG.debug("Property [{}] not found in known property list", propertyName);
//                return null;
//        }
//    }

//    @Override
//    public String getKnownPropertyValue(String domainCode, String propertyName) {
//        return getKnownPropertyValue(propertyName);
//    }

//    @Override
//    public void setKnownPropertyValue(String propertyName, String propertyValue) {
//        setPropertyValue(propertyName, propertyValue, true);
//    }

//    @Override
//    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast) {
//        setPropertyValue(propertyName, propertyValue, broadcast);
//    }
//
//    @Override
//    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue) {
//        setPropertyValue(propertyName, propertyValue, true);
//    }
//
//    protected void setPropertyValue(String propertyName, String propertyValue, boolean broadcast) {
//        if (getKnownProperties().get(propertyName).isStoredGlobally()) {
//            super.setKnownPropertyValue(propertyName, propertyValue);
//            return;
//        }
//        setOwnPropertyValue(propertyName, propertyValue, broadcast);
//    }

//    protected void setOwnPropertyValue(String propertyName, String propertyValue, boolean broadcast) {
//
//        Boolean value = Boolean.valueOf(propertyValue);
//        switch (propertyName) {
//            case SCHEMA_VALIDATION_ENABLED_PROPERTY:
//                this.setSchemaValidationEnabled(value);
//                break;
//            case MTOM_ENABLED_PROPERTY:
//                this.setMtomEnabled(value);
//                break;
//            default:
//                LOG.debug("Property [{}] cannot be set because it is not found", propertyName);
//                return;
//        }
//
//        LOG.debug("Signaling property value changed for [{}] property, broadcast: [{}]", propertyName);
//        DomainDTO currDomain = domainContextProvider.getCurrentDomainSafely();
//        pluginPropertyChangeNotifier.signalPropertyValueChanged(currDomain.getCode(), propertyName, propertyValue, broadcast);
//    }

//    private Boolean isMtomEnabled() {
//        return ((SOAPBinding) backendInterfaceEndpoint.getBinding()).isMTOMEnabled();
//    }
//
//    private void setMtomEnabled(Boolean flag) {
//        ((SOAPBinding) backendInterfaceEndpoint.getBinding()).setMTOMEnabled(flag);
//    }
//
//    private Boolean isSchemaValidationEnabled() {
//        return "true".equals(backendInterfaceEndpoint.getProperties().get("schema-validation-enabled"));
//    }
//
//    private void setSchemaValidationEnabled(Boolean flag) {
//        backendInterfaceEndpoint.getProperties().put("schema-validation-enabled", flag.toString());
//    }

}