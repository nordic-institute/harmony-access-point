package eu.domibus.core.jms;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.PluginMessageListenerContainer;
import eu.domibus.core.jms.multitenancy.DomainMessageListenerContainer;
import eu.domibus.core.jms.multitenancy.DomainMessageListenerContainerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Ion Perpegel
 * @author Cosmin Baciu
 * @since 4.0
 *
 * Class that manages the creation, reset and destruction of message listener containers
 * The instances are kept so that they can be recreated on property changes at runtime and also stoped at shutdown
 */
@Service
public class MessageListenerContainerInitializer {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageListenerContainerInitializer.class);

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    protected DomainMessageListenerContainerFactory messageListenerContainerFactory;

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomainExtConverter domainConverter;

    protected List<MessageListenerContainer> instances = new ArrayList<>();

    @PostConstruct
    public void init() {
        final List<Domain> domains = domainService.getDomains();
        for (Domain domain : domains) {
            createSendMessageListenerContainer(domain);
            createSendLargeMessageListenerContainer(domain);
            createSplitAndJoinListenerContainer(domain);
            createPullReceiptListenerContainer(domain);
            createPullMessageListenerContainer(domain);
            createRetentionListenerContainer(domain);

            createMessageListenersForPlugins(domain);
        }
    }

    @PreDestroy
    public void destroy() {
        LOG.info("Shutting down MessageListenerContainer instances");

        for (MessageListenerContainer instance : instances) {
            try {
                // There is an issue with destroying (shutting down) the message listener container on Tomcat while
                // stopping so we just stop the instances on the ApplicationContext shutdown (the invokers will still
                // close on JVM exit with warnings about connection being still in use on connection pool destroy)
                instance.stop();
            } catch (Exception e) {
                LOG.error("Error while shutting down MessageListenerContainer", e);
            }
        }
    }

    /**
     * It will collect and instantiate all {@link PluginMessageListenerContainer} defined in plugins
     *
     * @param domain
     */
    protected void createMessageListenersForPlugins(Domain domain) {
        DomainDTO domainDTO = domainConverter.convert(domain, DomainDTO.class);

        final Map<String, PluginMessageListenerContainer> beansOfType = applicationContext.getBeansOfType(PluginMessageListenerContainer.class);

        for (Map.Entry<String, PluginMessageListenerContainer> entry : beansOfType.entrySet()) {
            final String pluginMessageListenerContainerName = entry.getKey();
            final PluginMessageListenerContainer pluginMessageListenerContainer = entry.getValue();

            MessageListenerContainer instance = pluginMessageListenerContainer.createMessageListenerContainer(domainDTO);
            instance.start();
            instances.add(instance);
            LOG.info("{} initialized for domain [{}]", pluginMessageListenerContainerName, domain);
        }
    }

    protected void createSendMessageListenerContainer(Domain domain) {
        DomainMessageListenerContainer instance = messageListenerContainerFactory.createSendMessageListenerContainer(domain);
        instance.start();
        instances.add(instance);
        LOG.info("MessageListenerContainer initialized for domain [{}]", domain);
    }

    protected void createSendLargeMessageListenerContainer(Domain domain) {
        DomainMessageListenerContainer instance = messageListenerContainerFactory.createSendLargeMessageListenerContainer(domain);
        instance.start();
        instances.add(instance);
        LOG.info("LargeMessageListenerContainer initialized for domain [{}]", domain);
    }

    protected void createPullReceiptListenerContainer(Domain domain) {
        DomainMessageListenerContainer instance = messageListenerContainerFactory.createPullReceiptListenerContainer(domain);
        instance.start();
        instances.add(instance);
        LOG.info("PullReceiptListenerContainer initialized for domain [{}]", domain);
    }

    protected void createSplitAndJoinListenerContainer(Domain domain) {
        DomainMessageListenerContainer instance = messageListenerContainerFactory.createSplitAndJoinListenerContainer(domain);
        instance.start();
        instances.add(instance);
        LOG.info("SplitAndJoinListenerContainer initialized for domain [{}]", domain);
    }

    protected void createRetentionListenerContainer(Domain domain) {
        DomainMessageListenerContainer instance = messageListenerContainerFactory.createRetentionListenerContainer(domain);
        instance.start();
        instances.add(instance);
        LOG.info("RetentionListenerContainer initialized for domain [{}]", domain);
    }

    protected void createPullMessageListenerContainer(Domain domain) {
        DomainMessageListenerContainer instance = messageListenerContainerFactory.createPullMessageListenerContainer(domain);
        instance.start();
        instances.add(instance);
        LOG.info("PullListenerContainer initialized for domain [{}]", domain);
    }

    public void setConcurrency(Domain domain, String beanName, String concurrency) {
        DomainMessageListenerContainer oldInstance = instances.stream()
                .filter(instance -> instance instanceof DomainMessageListenerContainer)
                .map(instance -> (DomainMessageListenerContainer) instance)
                .filter(instance -> domain.equals(instance.getDomain()))
                .filter(instance -> beanName.equals(instance.getName()))
                .findFirst().orElse(null);
        if (oldInstance != null) {
            oldInstance.stop();
            oldInstance.setConcurrency(concurrency);
            oldInstance.start();
        }
    }
}
