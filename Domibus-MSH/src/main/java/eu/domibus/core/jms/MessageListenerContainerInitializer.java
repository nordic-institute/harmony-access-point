package eu.domibus.core.jms;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainsAware;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.jms.multitenancy.DomainMessageListenerContainerFactory;
import eu.domibus.core.jms.multitenancy.DomainMessageListenerContainerImpl;
import eu.domibus.core.jms.multitenancy.MessageListenerContainerConfiguration;
import eu.domibus.core.message.UserMessagePriorityConfiguration;
import eu.domibus.core.message.UserMessagePriorityService;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.PluginMessageListenerContainer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
public class MessageListenerContainerInitializer implements DomainsAware {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageListenerContainerInitializer.class);
    public static final String JMS_PRIORITY = "JMSPriority";

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    protected DomainMessageListenerContainerFactory messageListenerContainerFactory;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomibusCoreMapper coreMapper;

    @Autowired
    protected UserMessagePriorityService userMessagePriorityService;

    protected List<MessageListenerContainer> instances = new ArrayList<>();

    @PostConstruct
    public void init() {
        final List<Domain> domains = domainService.getDomains();
        createListenerContainers(domains);
    }

    @Override
    public void onDomainAdded(final Domain domain) {
        createListenerContainers(domain);
    }

    @Override
    public void onDomainRemoved(Domain domain) {
        stopInstancesFor(domain);
    }

    private void createListenerContainers(List<Domain> domains) {
        for (Domain domain : domains) {
            createListenerContainers(domain);
        }
    }

    private void createListenerContainers(Domain domain) {
        createSendMessageListenerContainers(domain);
        createSendLargeMessageListenerContainer(domain);
        createSplitAndJoinListenerContainer(domain);
        createPullReceiptListenerContainer(domain);
        createPullMessageListenerContainer(domain);
        createEArchiveMessageListenerContainer(domain);
        createRetentionListenerContainer(domain);
        createMessageListenersForPlugins(domain);
    }

    @PreDestroy
    public void destroy() {
        stop(instances);
    }

    /**
     * It will collect and instantiate all {@link PluginMessageListenerContainer} defined in plugins
     *
     * @param domain
     */
    protected void createMessageListenersForPlugins(Domain domain) {
        DomainDTO domainDTO = coreMapper.domainToDomainDTO(domain);

        final Map<String, PluginMessageListenerContainer> beansOfType = applicationContext.getBeansOfType(PluginMessageListenerContainer.class);

        for (Map.Entry<String, PluginMessageListenerContainer> entry : beansOfType.entrySet()) {
            final String pluginMessageListenerContainerName = entry.getKey();
            final PluginMessageListenerContainer pluginMessageListenerContainer = entry.getValue();

            MessageListenerContainer instance = pluginMessageListenerContainer.createMessageListenerContainer(domainDTO);
            if (instance != null) { // if null domain is disabled
                instance.start();
                instances.add(instance);
                LOG.info("{} initialized for domain [{}]", pluginMessageListenerContainerName, domain);
            }
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
        createMessageListenersWithPriority(domain, MessageListenerContainerConfiguration.DISPATCH_CONTAINER, selectorForDefaultDispatcher, DOMIBUS_DISPATCHER_CONCURENCY);

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

        DomainMessageListenerContainerImpl instance = messageListenerContainerFactory.createSendMessageListenerContainer(domain, null, DOMIBUS_DISPATCHER_CONCURENCY);
        instance.start();
        instances.add(instance);
        LOG.info("MessageListenerContainer initialized for domain [{}]", domain);
    }

    protected void createMessageListenersWithPriority(Domain domain, String messageListenerName, String selector, String concurrencyPropertyName) {
        LOG.info("Initializing MessageListenerContainer for domain [{}] with selector [{}] and concurrency property [{}]", domain, selector, concurrencyPropertyName);

        DomainMessageListenerContainerImpl listenerContainer = messageListenerContainerFactory.createSendMessageListenerContainer(domain, selector, concurrencyPropertyName);
        listenerContainer.setBeanName(messageListenerName);
        listenerContainer.start();
        instances.add(listenerContainer);

        LOG.info("MessageListenerContainer initialized for domain [{}] with selector [{}] and concurrency property [{}]", domain, selector, concurrencyPropertyName);
    }

    protected void createSendLargeMessageListenerContainer(Domain domain) {
        DomainMessageListenerContainerImpl instance = messageListenerContainerFactory.createSendLargeMessageListenerContainer(domain);
        instance.start();
        instances.add(instance);
        LOG.info("LargeMessageListenerContainer initialized for domain [{}]", domain);
    }

    protected void createPullReceiptListenerContainer(Domain domain) {
        DomainMessageListenerContainerImpl instance = messageListenerContainerFactory.createPullReceiptListenerContainer(domain);
        instance.start();
        instances.add(instance);
        LOG.info("PullReceiptListenerContainer initialized for domain [{}]", domain);
    }

    protected void createSplitAndJoinListenerContainer(Domain domain) {
        DomainMessageListenerContainerImpl instance = messageListenerContainerFactory.createSplitAndJoinListenerContainer(domain);
        instance.start();
        instances.add(instance);
        LOG.info("SplitAndJoinListenerContainer initialized for domain [{}]", domain);
    }

    protected void createRetentionListenerContainer(Domain domain) {
        DomainMessageListenerContainerImpl instance = messageListenerContainerFactory.createRetentionListenerContainer(domain);
        instance.start();
        instances.add(instance);
        LOG.info("RetentionListenerContainer initialized for domain [{}]", domain);
    }

    protected void createPullMessageListenerContainer(Domain domain) {
        DomainMessageListenerContainerImpl instance = messageListenerContainerFactory.createPullMessageListenerContainer(domain);
        instance.start();
        instances.add(instance);
        LOG.info("PullListenerContainer initialized for domain [{}]", domain);
    }

    protected void createEArchiveMessageListenerContainer(Domain domain) {
        DomainMessageListenerContainerImpl instance = messageListenerContainerFactory.createEArchiveMessageListenerContainer(domain);
        instance.start();
        DomainMessageListenerContainerImpl instance1 = messageListenerContainerFactory.createEArchiveNotificationListenerContainer(domain);
        instance1.start();
        DomainMessageListenerContainerImpl instance2 = messageListenerContainerFactory.createEArchiveNotificationDlqListenerContainer(domain);
        instance2.start();
        instances.add(instance);
        instances.add(instance1);
        instances.add(instance2);
        LOG.info("EArchiveListenerContainer initialized for domain [{}]", domain);
    }

    public void setConcurrency(Domain domain, String beanName, String concurrency) {
        DomainMessageListenerContainerImpl instance = getInstanceByNameAndDomain(domain, beanName);
        if (instance != null) {
            instance.setConcurrency(concurrency);
        }
    }

    private DomainMessageListenerContainerImpl getInstanceByNameAndDomain(Domain domain, String beanName) {
        return instances.stream()
                .filter(instance -> instance instanceof DomainMessageListenerContainerImpl)
                .map(instance -> (DomainMessageListenerContainerImpl) instance)
                .filter(instance -> domain.equals(instance.getDomain()))
                .filter(instance -> beanName.equals(instance.getName()))
                .findFirst().orElse(null);
    }

    private List<MessageListenerContainer> getInstancesByDomain(Domain domain) {
        return instances.stream().filter(instance -> instance instanceof DomainMessageListenerContainerImpl)
                .map(instance -> (DomainMessageListenerContainerImpl) instance)
                .filter(instance -> domain.equals(instance.getDomain()))
                .map(instance -> (MessageListenerContainer) instance)
                .collect(Collectors.toList());
    }

    private void stop(List<MessageListenerContainer> instances) {
        LOG.info("Shutting down MessageListenerContainer instances");

        // There is an issue with destroying / shutting down the message listener containers on Tomcat while stopping
        // it, so we first stop all the instances and then shut them all down (stopping down all instances first seems
        // to be avoiding the issue related to the XA exceptions being thrown when the ApplicationContext shuts down,
        // even thought the shutdown operations stop the instances too, but this happens one listener at a time)
        instances.forEach(instance -> {
            stopInstance(instance);
        });

        instances.forEach(instance -> {
            shutdownInstance(instance);
        });
    }
    
    private void stopInstancesFor(Domain domain) {
        List<MessageListenerContainer> instances = getInstancesByDomain(domain);
        stop(instances);
    }

    private void shutdownInstance(MessageListenerContainer instance) {
        try {
            LOG.info("Shutting down MessageListenerContainer instance: {}", instance);
            ((AbstractMessageListenerContainer) instance).shutdown();
        } catch (Exception e) {
            LOG.error("Error while shutting down MessageListenerContainer", e);
        }
    }

    private void stopInstance(MessageListenerContainer instance) {
        try {
            LOG.info("Stopping MessageListenerContainer instance: {}", instance);
            instance.stop();
        } catch (Exception e) {
            LOG.error("Error while stopping MessageListenerContainer", e);
        }
    }

}
