package eu.domibus.plugin.webService.configuration;

import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.DomibusEnvironmentUtil;
import eu.domibus.plugin.notification.PluginAsyncNotificationConfiguration;
import eu.domibus.plugin.webService.impl.WebServiceIPluginmpl;
import eu.domibus.plugin.webService.impl.ClearAuthenticationMDCInterceptor;
import eu.domibus.plugin.webService.impl.CustomAuthenticationInterceptor;
import eu.domibus.plugin.webService.impl.WSPluginFaultOutInterceptor;
import eu.domibus.plugin.webService.logging.WSPluginLoggingEventSender;
import eu.domibus.plugin.webService.property.WSPluginPropertyManager;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.cxf.Bus;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.jms.Queue;
import javax.xml.ws.Endpoint;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class responsible for the configuration of the plugin, independent of any server
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class WSPluginConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginConfiguration.class);

    public static final String NOTIFY_BACKEND_QUEUE_JNDI = "jms/domibus.notification.webservice";
    public static final String DOMIBUS_LOGGING_PAYLOAD_PRINT = "domibus.logging.payload.print";
    public static final String DOMIBUS_LOGGING_CXF_LIMIT = "domibus.logging.cxf.limit";


    @Bean("backendWebservice")
    public WebServiceIPluginmpl createWSPlugin() {
        return new WebServiceIPluginmpl();
    }

    @Bean("webserviceAsyncPluginConfiguration")
    public PluginAsyncNotificationConfiguration pluginAsyncNotificationConfiguration(@Qualifier("notifyBackendWebServiceQueue") Queue notifyBackendWebServiceQueue,
                                                                                     WebServiceIPluginmpl backendWebService,
                                                                                     Environment environment) {
        PluginAsyncNotificationConfiguration pluginAsyncNotificationConfiguration = new PluginAsyncNotificationConfiguration(backendWebService, notifyBackendWebServiceQueue);
        if (DomibusEnvironmentUtil.INSTANCE.isApplicationServer(environment)) {
            String queueNotificationJndi = NOTIFY_BACKEND_QUEUE_JNDI;
            LOG.debug("Domibus is running inside an application server. Setting the queue name to [{}]", queueNotificationJndi);
            pluginAsyncNotificationConfiguration.setQueueName(queueNotificationJndi);
        }
        return pluginAsyncNotificationConfiguration;
    }

    @Bean("wsPluginLoggingEventSender")
    public WSPluginLoggingEventSender wsPluginLoggingEventSender(DomibusPropertyExtService domibusPropertyExtService) {
        String payloadPrintString = domibusPropertyExtService.getProperty(DOMIBUS_LOGGING_PAYLOAD_PRINT);
        LOG.debug("Property [{}] value is [{}]", DOMIBUS_LOGGING_PAYLOAD_PRINT, payloadPrintString);

        WSPluginLoggingEventSender wsPluginLoggingEventSender = new WSPluginLoggingEventSender();
        wsPluginLoggingEventSender.setPrintPayload(BooleanUtils.toBoolean(payloadPrintString));
        return wsPluginLoggingEventSender;
    }

    @Bean("backendInterfaceEndpoint")
    public Endpoint backendInterfaceEndpoint(@Qualifier(Bus.DEFAULT_BUS_ID) Bus bus,
                                             WebServiceIPluginmpl backendWebService,
                                             WSPluginPropertyManager wsPluginPropertyManager,
                                             CustomAuthenticationInterceptor customAuthenticationInterceptor,
                                             ClearAuthenticationMDCInterceptor clearAuthenticationMDCInterceptor,
                                             WSPluginFaultOutInterceptor wsPluginFaultOutInterceptor,
                                             @Qualifier("wsLoggingFeature") LoggingFeature wsLoggingFeature) {
        EndpointImpl endpoint = new EndpointImpl(bus, backendWebService);
        Map<String, Object> endpointProperties = getEndpointProperties(wsPluginPropertyManager);
        endpoint.setProperties(endpointProperties);
        endpoint.setSchemaLocations(getSchemaLocations());
        endpoint.setInInterceptors(Arrays.asList(customAuthenticationInterceptor));
        endpoint.setOutInterceptors(Arrays.asList(clearAuthenticationMDCInterceptor));
        endpoint.setOutFaultInterceptors(Arrays.asList(wsPluginFaultOutInterceptor, clearAuthenticationMDCInterceptor));
        endpoint.setFeatures(Arrays.asList(wsLoggingFeature));

        endpoint.publish("/backend");
        return endpoint;
    }

    @Bean("wsLoggingFeature")
    public LoggingFeature wsLoggingFeature(WSPluginLoggingEventSender wsPluginLoggingEventSender,
                                           DomibusPropertyExtService domibusPropertyExtService) {
        LoggingFeature loggingFeature = new LoggingFeature();
        loggingFeature.setSender(wsPluginLoggingEventSender);

        Integer loggingLimit = domibusPropertyExtService.getIntegerProperty(DOMIBUS_LOGGING_CXF_LIMIT);
        LOG.debug("Using logging limit [{}]", loggingLimit);
        loggingFeature.setLimit(loggingLimit);

        return loggingFeature;
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
