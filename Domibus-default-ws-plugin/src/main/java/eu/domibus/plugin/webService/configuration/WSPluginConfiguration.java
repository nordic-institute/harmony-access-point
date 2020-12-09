package eu.domibus.plugin.webService.configuration;

import eu.domibus.common.NotificationType;
import eu.domibus.ext.services.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.DomibusEnvironmentUtil;
import eu.domibus.plugin.notification.PluginAsyncNotificationConfiguration;
import eu.domibus.plugin.webService.backend.dispatch.WSPluginBackendService;
import eu.domibus.plugin.webService.connector.WSPluginImpl;
import eu.domibus.plugin.webService.dao.WSMessageLogDao;
import eu.domibus.plugin.webService.impl.*;
import eu.domibus.plugin.webService.logging.WSPluginLoggingEventSender;
import eu.domibus.plugin.webService.property.WSPluginPropertyManager;
import org.apache.cxf.Bus;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
public class WSPluginConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginConfiguration.class);

    public static final String NOTIFY_BACKEND_QUEUE_JNDI = "jms/domibus.notification.webservice";
    public static final String DOMIBUS_LOGGING_PAYLOAD_PRINT = "domibus.logging.payload.print";
    public static final String DOMIBUS_LOGGING_METADATA_PRINT = "domibus.logging.metadata.print";
    public static final String DOMIBUS_LOGGING_CXF_LIMIT = "domibus.logging.cxf.limit";

    @Bean("backendWSPlugin")
    public WSPluginImpl createBackendJMSImpl(DomibusPropertyExtService domibusPropertyExtService,
                                             StubDtoTransformer defaultTransformer,
                                             WSMessageLogDao wsMessageLogDao,
                                             WSPluginBackendService wsPluginBackendService) {
        List<NotificationType> messageNotifications = domibusPropertyExtService.getConfiguredNotifications(WSPluginPropertyManager.MESSAGE_NOTIFICATIONS);
        LOG.debug("Using the following message notifications [{}]", messageNotifications);
        WSPluginImpl jmsPlugin = new WSPluginImpl(defaultTransformer, wsMessageLogDao, wsPluginBackendService);
        jmsPlugin.setRequiredNotifications(messageNotifications);
        return jmsPlugin;
    }

    @Bean("backendWebservice")
    public WebServicePluginImpl createWSPlugin(MessageAcknowledgeExtService messageAcknowledgeExtService,
                                               WebServicePluginExceptionFactory webServicePluginExceptionFactory,
                                               WSMessageLogDao wsMessageLogDao,
                                               DomainContextExtService domainContextExtService,
                                               WSPluginPropertyManager wsPluginPropertyManager,
                                               AuthenticationExtService authenticationExtService,
                                               MessageExtService messageExtService,
                                               WSPluginImpl wsPlugin) {
        return new WebServicePluginImpl(messageAcknowledgeExtService,
                webServicePluginExceptionFactory,
                wsMessageLogDao,
                domainContextExtService,
                wsPluginPropertyManager,
                authenticationExtService,
                messageExtService,
                wsPlugin);
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

    @Bean("wsPluginLoggingEventSender")
    public WSPluginLoggingEventSender wsPluginLoggingEventSender(DomibusPropertyExtService domibusPropertyExtService) {
        Boolean payloadPrint = domibusPropertyExtService.getBooleanProperty(DOMIBUS_LOGGING_PAYLOAD_PRINT);
        LOG.debug("Property [{}] value is [{}]", DOMIBUS_LOGGING_PAYLOAD_PRINT, payloadPrint);
        Boolean metadataPrint = domibusPropertyExtService.getBooleanProperty(DOMIBUS_LOGGING_METADATA_PRINT);
        LOG.debug("Property [{}] value is [{}]", DOMIBUS_LOGGING_METADATA_PRINT, metadataPrint);

        WSPluginLoggingEventSender wsPluginLoggingEventSender = new WSPluginLoggingEventSender();
        wsPluginLoggingEventSender.setPrintPayload(payloadPrint);
        wsPluginLoggingEventSender.setPrintMetadata(metadataPrint);
        return wsPluginLoggingEventSender;
    }

    @Bean("backendInterfaceEndpoint")
    public Endpoint backendInterfaceEndpoint(@Qualifier(Bus.DEFAULT_BUS_ID) Bus bus,
                                             WebServicePluginImpl backendWebService,
                                             WSPluginPropertyManager wsPluginPropertyManager,
                                             CustomAuthenticationInterceptor customAuthenticationInterceptor,
                                             ClearAuthenticationMDCInterceptor clearAuthenticationMDCInterceptor,
                                             WSPluginFaultOutInterceptor wsPluginFaultOutInterceptor,
                                             @Qualifier("wsLoggingFeature") LoggingFeature wsLoggingFeature) {
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
