package eu.domibus.plugin.ws.webservice.configuration;

import eu.domibus.ext.services.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.DomibusEnvironmentUtil;
import eu.domibus.plugin.notification.PluginAsyncNotificationConfiguration;
import eu.domibus.plugin.webService.impl.HttpMethodAuthorizationInInterceptor;
import eu.domibus.plugin.ws.backend.WSBackendMessageLogService;
import eu.domibus.plugin.ws.backend.dispatch.WSPluginBackendService;
import eu.domibus.plugin.ws.backend.reliability.queue.WSSendMessageListenerContainer;
import eu.domibus.plugin.ws.connector.WSPluginImpl;
import eu.domibus.plugin.ws.initialize.WSPluginInitializer;
import eu.domibus.plugin.ws.message.WSMessageLogService;
import eu.domibus.plugin.ws.property.WSPluginPropertyManager;
import eu.domibus.plugin.ws.webservice.StubDtoTransformer;
import eu.domibus.plugin.ws.webservice.WebServiceExceptionFactory;
import eu.domibus.plugin.ws.webservice.WebServiceImpl;
import eu.domibus.plugin.ws.webservice.interceptor.ClearAuthenticationMDCInterceptor;
import eu.domibus.plugin.ws.webservice.interceptor.CustomAuthenticationInterceptor;
import eu.domibus.plugin.ws.webservice.interceptor.WebServiceFaultOutInterceptor;
import org.apache.cxf.Bus;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

import javax.jms.Queue;
import javax.xml.ws.Endpoint;
import java.util.*;

/**
 * Class responsible for the configuration of the plugin, independent of any server
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class WebServiceConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WebServiceConfiguration.class);

    public static final String NOTIFY_BACKEND_QUEUE_JNDI = "jms/domibus.notification.webservice";
    public static final String BACKEND_INTERFACE_ENDPOINT_BEAN_NAME = "backendInterfaceEndpoint";

    @Bean(WSPluginImpl.PLUGIN_NAME)
    public WSPluginImpl createBackendJMSImpl(StubDtoTransformer defaultTransformer,
                                             WSMessageLogService wsMessageLogService,
                                             WSPluginBackendService wsPluginBackendService,
                                             WSPluginPropertyManager wsPluginPropertyManager,
                                             WSSendMessageListenerContainer wsSendMessageListenerContainer,
                                             DomibusPropertyExtService domibusPropertyExtService,
                                             @Lazy WSPluginInitializer wsPluginInitializer //use lazy initialization to avoid circular dependency
                                             //triggered by the usage of the WSPlugin when publishing the endpoints
    ) {
        WSPluginImpl wsPlugin = new WSPluginImpl(defaultTransformer, wsMessageLogService, wsPluginBackendService,
                wsPluginPropertyManager, wsSendMessageListenerContainer, domibusPropertyExtService, wsPluginInitializer);
        return wsPlugin;
    }

    @Bean("backendWebservice")
    public WebServiceImpl createWSPlugin(MessageAcknowledgeExtService messageAcknowledgeExtService,
                                         WebServiceExceptionFactory webServicePluginExceptionFactory,
                                         WSMessageLogService wsMessageLogService,
                                         WSBackendMessageLogService wsBackendMessageLogService,
                                         DomainContextExtService domainContextExtService,
                                         WSPluginPropertyManager wsPluginPropertyManager,
                                         AuthenticationExtService authenticationExtService,
                                         MessageExtService messageExtService,
                                         WSPluginImpl wsPlugin,
                                         DateExtService dateUtil) {
        return new WebServiceImpl(messageAcknowledgeExtService,
                webServicePluginExceptionFactory,
                wsMessageLogService,
                wsBackendMessageLogService,
                domainContextExtService,
                wsPluginPropertyManager,
                authenticationExtService,
                messageExtService,
                wsPlugin,
                dateUtil);
    }


    @Bean("webserviceAsyncPluginConfiguration")
    public PluginAsyncNotificationConfiguration pluginAsyncNotificationConfiguration(@Qualifier("notifyBackendWebServiceQueue") Queue notifyBackendWebServiceQueue,
                                                                                     WSPluginImpl wsPlugin,
                                                                                     Environment environment) {
        PluginAsyncNotificationConfiguration pluginAsyncNotificationConfiguration = new PluginAsyncNotificationConfiguration(wsPlugin, notifyBackendWebServiceQueue);
        if (DomibusEnvironmentUtil.INSTANCE.isApplicationServer(environment)) {
            String queueNotificationJndi = NOTIFY_BACKEND_QUEUE_JNDI;
            LOG.debug("Domibus is running inside an application server. Setting the queue name to [{}]", queueNotificationJndi);
            pluginAsyncNotificationConfiguration.setQueueName(queueNotificationJndi);
        }
        return pluginAsyncNotificationConfiguration;
    }

    @Bean(BACKEND_INTERFACE_ENDPOINT_BEAN_NAME)
    public Endpoint backendInterfaceEndpoint(@Qualifier(Bus.DEFAULT_BUS_ID) Bus bus,
                                             WebServiceImpl backendWebService,
                                             WSPluginPropertyManager wsPluginPropertyManager,
                                             HttpMethodAuthorizationInInterceptor httpMethodAuthorizationInInterceptor,
                                             CustomAuthenticationInterceptor customAuthenticationInterceptor,
                                             ClearAuthenticationMDCInterceptor clearAuthenticationMDCInterceptor,
                                             WebServiceFaultOutInterceptor wsPluginFaultOutInterceptor,
                                             @Qualifier("wsLoggingFeature") LoggingFeature wsLoggingFeature) {
        EndpointImpl endpoint = new EndpointImpl(bus, backendWebService); //NOSONAR
        Map<String, Object> endpointProperties = getEndpointProperties(wsPluginPropertyManager);
        endpoint.setProperties(endpointProperties);
        endpoint.setSchemaLocations(getSchemaLocations());
        endpoint.setInInterceptors(Arrays.asList(httpMethodAuthorizationInInterceptor, customAuthenticationInterceptor));
        endpoint.setOutInterceptors(Collections.singletonList(clearAuthenticationMDCInterceptor));
        endpoint.setOutFaultInterceptors(Arrays.asList(wsPluginFaultOutInterceptor, clearAuthenticationMDCInterceptor));
        endpoint.setFeatures(Collections.singletonList(wsLoggingFeature));


        return endpoint;
    }

    private List<String> getSchemaLocations() {
        return Arrays.asList(
                "schemas/webservicePlugin-body.xsd",
                "schemas/webservicePlugin-header.xsd",
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
