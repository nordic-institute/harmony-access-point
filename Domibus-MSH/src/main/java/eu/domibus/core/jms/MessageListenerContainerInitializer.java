package eu.domibus.core.jms;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyMetadataManager;
import eu.domibus.core.jms.multitenancy.DomainMessageListenerContainer;
import eu.domibus.core.jms.multitenancy.DomainMessageListenerContainerFactory;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.PluginMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.listener.AbstractJmsListeningContainer;
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
 * <p>
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
            createDlqListenerContainers(domain);
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
    public void destroy() throws InterruptedException {
        LOG.info("Shutting down MessageListenerContainer instances");

        for (MessageListenerContainer instance : instances) {
            try {
                ((AbstractJmsListeningContainer) instance).shutdown();
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
    public void createMessageListenersForPlugins(Domain domain) {
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

    public void createSendMessageListenerContainer(Domain domain) {
        DomainMessageListenerContainer instance = messageListenerContainerFactory.createSendMessageListenerContainer(domain);
        removeInstance(domain, instance.getName());
        instance.start();
        instances.add(instance);
        LOG.info("MessageListenerContainer initialized for domain [{}]", domain);
    }

    public void createDlqListenerContainers(Domain domain) {
        /*for (int index = 0; index < 20; index++) {
            DomainMessageListenerContainer priorityListener = messageListenerContainerFactory.createDlqListenerContainerMediumPriority(domain, "messagePriority = '10" + index + "'", DomibusPropertyMetadataManager.DOMIBUS_DLQ_CONCURENCY_MEDIUM);
            priorityListener.start();
            instances.add(priorityListener);
        }*/

        DomainMessageListenerContainer lowPriority = messageListenerContainerFactory.createDlqListenerContainerLowPriority(domain, "messagePriority = '1'", DomibusPropertyMetadataManager.DOMIBUS_DLQ_CONCURENCY_LOW);
        removeInstance(domain, lowPriority.getName());
        lowPriority.start();
        instances.add(lowPriority);

        DomainMessageListenerContainer mediumPriority = messageListenerContainerFactory.createDlqListenerContainerMediumPriority(domain, "messagePriority = '5'", DomibusPropertyMetadataManager.DOMIBUS_DLQ_CONCURENCY_MEDIUM);
        removeInstance(domain, mediumPriority.getName());
        mediumPriority.start();
        instances.add(mediumPriority);

        DomainMessageListenerContainer highPriority = messageListenerContainerFactory.createDlqListenerContainerHighPriority(domain, "messagePriority = '10'", DomibusPropertyMetadataManager.DOMIBUS_DLQ_CONCURENCY_HIGH);
        removeInstance(domain, highPriority.getName());
        highPriority.start();
        instances.add(highPriority);



        LOG.info("MessageListenerContainer initialized for domain [{}]", domain);
    }

    public void createSendLargeMessageListenerContainer(Domain domain) {
        DomainMessageListenerContainer instance = messageListenerContainerFactory.createSendLargeMessageListenerContainer(domain);
        removeInstance(domain, instance.getName());
        instance.start();
        instances.add(instance);
        LOG.info("LargeMessageListenerContainer initialized for domain [{}]", domain);
    }

    public void createPullReceiptListenerContainer(Domain domain) {
        DomainMessageListenerContainer instance = messageListenerContainerFactory.createPullReceiptListenerContainer(domain);
        removeInstance(domain, instance.getName());
        instance.start();
        instances.add(instance);
        LOG.info("PullReceiptListenerContainer initialized for domain [{}]", domain);
    }

    public void createSplitAndJoinListenerContainer(Domain domain) {
        DomainMessageListenerContainer instance = messageListenerContainerFactory.createSplitAndJoinListenerContainer(domain);
        removeInstance(domain, instance.getName());
        instance.start();
        instances.add(instance);
        LOG.info("SplitAndJoinListenerContainer initialized for domain [{}]", domain);
    }

    public void createRetentionListenerContainer(Domain domain) {
        DomainMessageListenerContainer instance = messageListenerContainerFactory.createRetentionListenerContainer(domain);
        removeInstance(domain, instance.getName());
        instance.start();
        instances.add(instance);
        LOG.info("RetentionListenerContainer initialized for domain [{}]", domain);
    }

    public void createPullMessageListenerContainer(Domain domain) {
        DomainMessageListenerContainer instance = messageListenerContainerFactory.createPullMessageListenerContainer(domain);
        removeInstance(domain, instance.getName());
        instance.start();
        instances.add(instance);
        LOG.info("PullListenerContainer initialized for domain [{}]", domain);
    }

    private void removeInstance(Domain domain, String beanName) {
        DomainMessageListenerContainer oldInstance = instances.stream()
                .filter(instance -> instance instanceof DomainMessageListenerContainer)
                .map(instance -> (DomainMessageListenerContainer) instance)
                .filter(instance -> domain.equals(instance.getDomain()))
                .filter(instance -> beanName.equals(instance.getName()))
                .findFirst().orElse(null);
        if (oldInstance != null) {
            try {
                oldInstance.shutdown();
            } catch (Exception e) {
                LOG.error("Error while shutting down [{}] MessageListenerContainer for domain [{}]", beanName, domain, e);
            }
            instances.remove(oldInstance);
        }
    }

}
