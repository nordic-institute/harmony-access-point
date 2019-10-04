package eu.domibus.plugin.fs.queue;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.PluginMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * FSPlugin Out {@code MessageListenerContainer} which implements {@code PluginMessageListenerContainer}
 *
 * @author Catalin Enache
 * @since 4.1
 */
@Service
public class FSSendMessageListenerContainer implements PluginMessageListenerContainer {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSSendMessageListenerContainer.class);

    @Autowired
    protected ApplicationContext applicationContext;

    protected Map<DomainDTO, DefaultMessageListenerContainer> instances = new HashMap<>();

    @Override
    public DefaultMessageListenerContainer createMessageListenerContainer(DomainDTO domain) {
        LOG.debug("Creating the FSSendMessageListenerContainer  for domain [{}]", domain);
        DefaultMessageListenerContainer instance = (DefaultMessageListenerContainer) applicationContext.getBean("fsPluginOutContainer", domain);
        instances.put(domain, instance);
        return instance;
    }

    @Override
    public void updateMessageListenerContainerConcurrency(DomainDTO domain, String concurrency) {
        DefaultMessageListenerContainer instance = instances.get(domain);
        if (instance == null) {
            LOG.warn("fsPluginOutContainer instance not found for domain [{}]", domain);
            return;
        }
        instance.setConcurrency(concurrency);
    }
}
