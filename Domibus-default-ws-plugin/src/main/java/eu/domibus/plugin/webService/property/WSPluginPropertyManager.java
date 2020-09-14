package eu.domibus.plugin.webService.property;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO.Type;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO.Usage;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomibusPropertyExtServiceDelegateAbstract;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Property manager for ws plugin, now forwards all calls to domibus property provider as all his properties are managed in core
 *
 * @author Ion Perpegel
 * @since 4.1
 */
@Component
public class WSPluginPropertyManager extends DomibusPropertyExtServiceDelegateAbstract
        implements DomibusPropertyManagerExt {

    public static final String SCHEMA_VALIDATION_ENABLED_PROPERTY = "wsplugin.schema.validation.enabled";
    public static final String MTOM_ENABLED_PROPERTY = "wsplugin.mtom.enabled";
    public static final String PROP_LIST_PENDING_MESSAGES_MAXCOUNT = "wsplugin.messages.pending.list.max";


    private Map<String, DomibusPropertyMetadataDTO> knownProperties;

    public WSPluginPropertyManager() {
        List<DomibusPropertyMetadataDTO> allProperties = Arrays.asList(
                new DomibusPropertyMetadataDTO(SCHEMA_VALIDATION_ENABLED_PROPERTY, Type.BOOLEAN, Module.WS_PLUGIN, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(MTOM_ENABLED_PROPERTY, Type.BOOLEAN, Module.WS_PLUGIN, Usage.GLOBAL),
                new DomibusPropertyMetadataDTO(PROP_LIST_PENDING_MESSAGES_MAXCOUNT, Type.NUMERIC, Module.WS_PLUGIN, Usage.GLOBAL)
        );
        knownProperties = allProperties.stream().collect(Collectors.toMap(x -> x.getName(), x -> x));
    }

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        return knownProperties;
    }
}