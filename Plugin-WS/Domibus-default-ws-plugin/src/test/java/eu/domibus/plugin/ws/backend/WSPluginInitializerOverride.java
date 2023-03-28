package eu.domibus.plugin.ws.backend;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.ws.initialize.WSPluginInitializer;
import eu.domibus.plugin.ws.webservice.configuration.WebServiceConfiguration;
import eu.domibus.plugin.ws.webservice.deprecated.WSPluginConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.xml.ws.Endpoint;

/**
 * @since 5.1.1
 * @author Cosmin Baciu
 */
@Service
@Primary
public class WSPluginInitializerOverride extends WSPluginInitializer {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginInitializerOverride.class);

    private boolean endpointsInitialized;


    public WSPluginInitializerOverride(@Qualifier(WebServiceConfiguration.BACKEND_INTERFACE_ENDPOINT_BEAN_NAME) Endpoint wsPlugin,
                                       @Qualifier(WSPluginConfiguration.BACKEND_INTERFACE_ENDPOINT_DEPRECATED_BEAN_NAME) Endpoint wsPluginDeprecated) {
        super(wsPlugin, wsPluginDeprecated);
    }
    @Override
    public void initializeNonSynchronized() {
        if(!endpointsInitialized) {
            LOG.info("initializeNonSynchronized from IT test");
            super.initializeNonSynchronized();
            endpointsInitialized = true;
        }
    }
}
