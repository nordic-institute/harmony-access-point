package eu.domibus.plugin.ws.initialize;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.initialize.PluginInitializer;
import eu.domibus.plugin.ws.connector.WSPluginImpl;
import eu.domibus.plugin.ws.webservice.configuration.WebServiceConfiguration;
import eu.domibus.plugin.ws.webservice.deprecated.WSPluginConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.xml.ws.Endpoint;

/**
 * @since 5.1.1
 * @author Cosmin Baciu
 */
@Service
public class WSPluginInitializer implements PluginInitializer {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginInitializer.class);

    protected Endpoint wsPlugin;
    protected Endpoint wsPluginDeprecated;

    public WSPluginInitializer(@Qualifier(WebServiceConfiguration.BACKEND_INTERFACE_ENDPOINT_BEAN_NAME) Endpoint wsPlugin,
                               @Qualifier(WSPluginConfiguration.BACKEND_INTERFACE_ENDPOINT_DEPRECATED_BEAN_NAME) Endpoint wsPluginDeprecated) {
        this.wsPlugin = wsPlugin;
        this.wsPluginDeprecated = wsPluginDeprecated;
    }

    @Override
    public String getName() {
        return WSPluginImpl.PLUGIN_NAME;
    }

    @Override
    public void initializeNonSynchronized() {
        LOG.info("Publishing the WS Plugin endpoints");

        wsPlugin.publish("/wsplugin");
        wsPluginDeprecated.publish("/backend");
    }

    @Override
    public void initializeWithLockIfNeeded() {

    }
}
