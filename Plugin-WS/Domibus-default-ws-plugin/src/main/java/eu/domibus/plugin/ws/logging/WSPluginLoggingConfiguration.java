package eu.domibus.plugin.ws.logging;

import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author François Gautier
 * @since 5.1
 */
@Configuration
public class WSPluginLoggingConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginLoggingConfiguration.class);

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
}
