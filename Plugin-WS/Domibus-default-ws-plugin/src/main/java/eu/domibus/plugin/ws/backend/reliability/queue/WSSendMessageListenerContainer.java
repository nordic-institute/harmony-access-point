package eu.domibus.plugin.ws.backend.reliability.queue;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.PluginMessageListenerContainer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static eu.domibus.plugin.ws.backend.reliability.queue.WSMessageListenerContainerConfiguration.WS_PLUGIN_OUT_CONTAINER;

/**
 * WSPlugin Out {@code MessageListenerContainer} which implements {@code PluginMessageListenerContainer}
 *
 * @author François Gautier
 * @since 5.0
 */
@Service
public class WSSendMessageListenerContainer implements PluginMessageListenerContainer {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSSendMessageListenerContainer.class);

    protected ObjectProvider<DefaultMessageListenerContainer> wsPluginOutContainerProvider;

    protected Map<DomainDTO, DefaultMessageListenerContainer> instances = new HashMap<>();

    public WSSendMessageListenerContainer(@Qualifier(WS_PLUGIN_OUT_CONTAINER) ObjectProvider<DefaultMessageListenerContainer> wsPluginOutContainerProvider) {
        this.wsPluginOutContainerProvider = wsPluginOutContainerProvider;
    }

    @Override
    public DefaultMessageListenerContainer createMessageListenerContainer(DomainDTO domain) {
        LOG.debug("Creating the WSSendMessageListenerContainer for domain [{}]", domain);
        DefaultMessageListenerContainer instance = wsPluginOutContainerProvider.getObject(domain);
        instances.put(domain, instance);
        return instance;
    }

    @Override
    public void updateMessageListenerContainerConcurrency(DomainDTO domain, String concurrency) {
        DefaultMessageListenerContainer instance = instances.get(domain);
        if (instance == null) {
            LOG.warn("wsPluginOutContainer instance not found for domain [{}]", domain);
            return;
        }
        instance.setConcurrency(concurrency);
    }
}
