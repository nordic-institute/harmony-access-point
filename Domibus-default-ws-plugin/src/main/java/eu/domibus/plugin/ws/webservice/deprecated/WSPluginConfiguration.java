package eu.domibus.plugin.ws.webservice.deprecated;

import eu.domibus.ext.services.AuthenticationExtService;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.MessageAcknowledgeExtService;
import eu.domibus.ext.services.MessageExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.ws.connector.WSPluginImpl;
import eu.domibus.plugin.ws.property.WSPluginPropertyManager;
import eu.domibus.plugin.ws.webservice.WSMessageLogDao;
import eu.domibus.plugin.ws.webservice.deprecated.mapper.WSPluginMessagingMapper;
import eu.domibus.plugin.ws.webservice.interceptor.ClearAuthenticationMDCInterceptor;
import eu.domibus.plugin.ws.webservice.interceptor.CustomAuthenticationInterceptor;
import eu.domibus.plugin.ws.webservice.interceptor.WebServiceFaultOutInterceptor;
import org.apache.cxf.Bus;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.ws.Endpoint;
import java.util.*;

/**
 * Class responsible for the configuration of the plugin, independent of any server
 *
 * @author Cosmin Baciu
 * @since 4.2
 * @deprecated since 5.0 Use instead {@link eu.domibus.plugin.ws.webservice.configuration.WebServiceConfiguration}
 */
@Deprecated
@Configuration(value = "WSPluginConfigurationDeprecated")
public class WSPluginConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginConfiguration.class);

    @Bean("backendWebserviceDeprecated")
    public WebServicePluginImpl createWSPlugin(MessageAcknowledgeExtService messageAcknowledgeExtService,
                                               WebServicePluginExceptionFactory webServicePluginExceptionFactory,
                                               WSMessageLogDao wsMessageLogDao,
                                               DomainContextExtService domainContextExtService,
                                               WSPluginPropertyManager wsPluginPropertyManager,
                                               AuthenticationExtService authenticationExtService,
                                               MessageExtService messageExtService,
                                               WSPluginImpl wsPlugin,
                                               WSPluginMessagingMapper messagingMapper) {
        return new WebServicePluginImpl(messageAcknowledgeExtService,
                webServicePluginExceptionFactory,
                wsMessageLogDao,
                domainContextExtService,
                wsPluginPropertyManager,
                authenticationExtService,
                messageExtService,
                wsPlugin,
                messagingMapper);
    }

    @Bean("backendInterfaceEndpointDeprecated")
    public Endpoint backendInterfaceEndpointDeprecated(@Qualifier(Bus.DEFAULT_BUS_ID) Bus bus,
                                                       WebServicePluginImpl backendWebService,
                                                       WSPluginPropertyManager wsPluginPropertyManager,
                                                       CustomAuthenticationInterceptor customAuthenticationInterceptor,
                                                       ClearAuthenticationMDCInterceptor clearAuthenticationMDCInterceptor,
                                                       WebServiceFaultOutInterceptor wsPluginFaultOutInterceptor,
                                                       @Qualifier("wsLoggingFeature") LoggingFeature wsLoggingFeature) {
        LOG.warn("This endpoint is deprecated, please use the new end point /wsPlugin");
        EndpointImpl endpoint = new EndpointImpl(bus, backendWebService); //NOSONAR
        Map<String, Object> endpointProperties = getEndpointProperties(wsPluginPropertyManager);
        endpoint.setProperties(endpointProperties);
        endpoint.setSchemaLocations(getSchemaLocations());
        endpoint.setInInterceptors(Collections.singletonList(customAuthenticationInterceptor));
        endpoint.setOutInterceptors(Collections.singletonList(clearAuthenticationMDCInterceptor));
        endpoint.setOutFaultInterceptors(Arrays.asList(wsPluginFaultOutInterceptor, clearAuthenticationMDCInterceptor));
        endpoint.setFeatures(Collections.singletonList(wsLoggingFeature));

        endpoint.publish("/backend");
        return endpoint;
    }

    private List<String> getSchemaLocations() {
        return Arrays.asList(
                "schemas/domibus-header.xsd",
                "schemas/domibus-backend.xsd",
                "schemas/xml.xsd",
                "schemas/xmlmime.xsd"
        );
    }

    protected Map<String, Object> getEndpointProperties(WSPluginPropertyManager wsPluginPropertyManager) {
        String schemaValidationEnabled = wsPluginPropertyManager.getKnownPropertyValue("wsplugin.schema.validation.enabled");
        LOG.debug("Is schema validation enabled [{}]?", schemaValidationEnabled);

        String mtomEnabled = wsPluginPropertyManager.getKnownPropertyValue("wsplugin.mtom.enabled");
        LOG.debug("Is MTOM enabled [{}]?", mtomEnabled);

        Map<String, Object> properties = new HashMap<>();
        properties.put("schema-validation-enabled", schemaValidationEnabled);
        properties.put("mtom-enabled", mtomEnabled);
        return properties;
    }
}
