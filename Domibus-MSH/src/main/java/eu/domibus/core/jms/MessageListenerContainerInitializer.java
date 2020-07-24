package eu.domibus.core.jms;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.jms.multitenancy.DomainMessageListenerContainer;
import eu.domibus.core.jms.multitenancy.DomainMessageListenerContainerFactory;
import eu.domibus.core.message.UserMessagePriorityConfiguration;
import eu.domibus.core.message.UserMessagePriorityService;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.PluginMessageListenerContainer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DISPATCHER_CONCURENCY;

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
    public static final String JMS_PRIORITY = "JMSPriority";

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    protected DomainMessageListenerContainerFactory messageListenerContainerFactory;

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomainExtConverter domainConverter;

    @Autowired
    protected UserMessagePriorityService userMessagePriorityService;

    protected List<MessageListenerContainer> instances = new ArrayList<>();

    @PostConstruct
    public void init() {
        final List<Domain> domains = domainService.getDomains();
        for (Domain domain : domains) {
            createSendMessageListenerContainers(domain);
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
                // stopping so we just stop the instances on the ApplicationContext shutdown (the invokers will
                // close on JVM exit)
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

    protected void createSendMessageListenerContainers(Domain domain) {
        LOG.info("Creating SendMessageListenerContainers");

        List<UserMessagePriorityConfiguration> configuredPrioritiesWithConcurrency = userMessagePriorityService.getConfiguredRulesWithConcurrency(domain);
        if (CollectionUtils.isEmpty(configuredPrioritiesWithConcurrency)) {
            createSendMessageListenerContainer(domain);
            return;
        }

        for (UserMessagePriorityConfiguration userMessagePriorityConfiguration : configuredPrioritiesWithConcurrency) {
            String selector = getSelectorForPriority(userMessagePriorityConfiguration.getPriority());
            createMessageListenersWithPriority(domain, userMessagePriorityConfiguration.getRuleName() + "dispatcher", selector, userMessagePriorityConfiguration.getConcurrencyPropertyName());
        }

        List<Integer> priorities = getPriorities(configuredPrioritiesWithConcurrency);
        String selectorForDefaultDispatcher = getSelectorForDefaultDispatcher(priorities);
        createMessageListenersWithPriority(domain, "dispatchContainer", selectorForDefaultDispatcher, DOMIBUS_DISPATCHER_CONCURENCY);

        LOG.info("Finished creating SendMessageListenerContainers");
    }

    protected String getSelectorForPriority(Integer priority) {
        return JMS_PRIORITY + "=" + priority;
    }

    protected List<Integer> getPriorities(List<UserMessagePriorityConfiguration> configuredPrioritiesWithConcurrency) {
        List<Integer> result = new ArrayList<>();
        for (UserMessagePriorityConfiguration userMessagePriorityConfiguration : configuredPrioritiesWithConcurrency) {
            result.add(userMessagePriorityConfiguration.getPriority());
        }
        return result;
    }

    protected String getSelectorForDefaultDispatcher(List<Integer> existingPriorities) {
        StringBuilder selector = new StringBuilder();
        selector.append("(JMSPriority is null or (");
        List<String> jmsPrioritySelectors = new ArrayList<>();
        for (Integer existingPriority : existingPriorities) {
            jmsPrioritySelectors.add(JMS_PRIORITY + "<>" + existingPriority);
        }
        String jmsPrioritySelector = StringUtils.join(jmsPrioritySelectors, " and ");
        selector.append(jmsPrioritySelector);

        selector.append(") )");
        LOG.debug("Determined selector [{}]", selector);

        return selector.toString();
    }

    public void createSendMessageListenerContainer(Domain domain) {
        LOG.info("Creating the SendMessageListenerContainer for domain [{}] ", domain);

        DomainMessageListenerContainer instance = messageListenerContainerFactory.createSendMessageListenerContainer(domain, null, DOMIBUS_DISPATCHER_CONCURENCY);
        removeInstance(domain, instance.getName());
        instance.start();
        instances.add(instance);
        LOG.info("MessageListenerContainer initialized for domain [{}]", domain);
    }

    protected void createMessageListenersWithPriority(Domain domain, String messageListenerName, String selector, String concurrencyPropertyName) {
        LOG.info("Initializing MessageListenerContainer for domain [{}] with selector [{}] and concurrency property [{}]", domain, selector, concurrencyPropertyName);

        DomainMessageListenerContainer listenerContainer = messageListenerContainerFactory.createSendMessageListenerContainer(domain, selector, concurrencyPropertyName);
        listenerContainer.setBeanName(messageListenerName);
        removeInstance(domain, listenerContainer.getName());
        listenerContainer.start();
        instances.add(listenerContainer);

        LOG.info("MessageListenerContainer initialized for domain [{}] with selector [{}] and concurrency property [{}]", domain, selector, concurrencyPropertyName);
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
