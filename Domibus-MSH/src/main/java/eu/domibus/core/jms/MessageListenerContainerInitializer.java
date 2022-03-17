package eu.domibus.core.jms;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainMessageListenerContainer;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainsAware;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.jms.multitenancy.DomainMessageListenerContainerFactory;
import eu.domibus.core.jms.multitenancy.DomainMessageListenerContainerImpl;
import eu.domibus.core.jms.multitenancy.MessageListenerContainerConfiguration;
import eu.domibus.core.jms.multitenancy.PluginDomainMessageListenerContainerAdapter;
import eu.domibus.core.message.UserMessagePriorityConfiguration;
import eu.domibus.core.message.UserMessagePriorityService;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.PluginMessageListenerContainer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
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

    protected List<DomainMessageListenerContainer> instances = new ArrayList<>();

    protected final ApplicationContext applicationContext;

    protected final DomainMessageListenerContainerFactory messageListenerContainerFactory;

    protected final DomibusPropertyProvider domibusPropertyProvider;

    protected final DomainService domainService;

    protected final DomibusCoreMapper coreMapper;

    protected final UserMessagePriorityService userMessagePriorityService;

    public MessageListenerContainerInitializer(ApplicationContext applicationContext,
                                               DomainMessageListenerContainerFactory messageListenerContainerFactory,
                                               DomibusPropertyProvider domibusPropertyProvider, DomainService domainService,
                                               DomibusCoreMapper coreMapper, UserMessagePriorityService userMessagePriorityService) {

        this.applicationContext = applicationContext;
        this.messageListenerContainerFactory = messageListenerContainerFactory;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domainService = domainService;
        this.coreMapper = coreMapper;
        this.userMessagePriorityService = userMessagePriorityService;

        final List<Domain> domains = domainService.getDomains();
        createInstancesFor(domains);
    }

    @Override
    public void onDomainAdded(final Domain domain) {
        createInstancesFor(domain);
    }

    @Override
    public void onDomainRemoved(Domain domain) {
        stopAndRemoveInstancesFor(domain);
    }

    private void createInstancesFor(List<Domain> domains) {
        for (Domain domain : domains) {
            createInstancesFor(domain);
        }
    }

    private void createInstancesFor(Domain domain) {
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
            final String name = entry.getKey();
            final PluginMessageListenerContainer containerFactory = entry.getValue();

            MessageListenerContainer instance = containerFactory.createMessageListenerContainer(domainDTO);
            // if null, domain is disabled
            if (instance == null) {
                LOG.info("Message listener container [{}] for domain [{}] returned null so exiting.", name, domain);
                return;
            }

            DomainMessageListenerContainer adapter = new PluginDomainMessageListenerContainerAdapter(instance, domain, name);
            instance.start();
            instances.add(adapter);
            LOG.info("{} initialized for domain [{}]", name, domain);
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
        DomainMessageListenerContainer instance = getInstanceByNameAndDomain(domain, beanName);
        if (instance != null) {
            instance.setConcurrency(concurrency);
        }
    }

    private DomainMessageListenerContainer getInstanceByNameAndDomain(Domain domain, String beanName) {
        return instances.stream()
                .filter(instance -> domain.equals(instance.getDomain()))
                .filter(instance -> beanName.equals(instance.getName()))
                .findFirst().orElse(null);
    }

    private List<DomainMessageListenerContainer> getInstancesByDomain(Domain domain) {
        return instances.stream()
                .filter(instance -> domain.equals(instance.getDomain()))
                .collect(Collectors.toList());
    }

    private void stop(List<DomainMessageListenerContainer> instances) {
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

    private void stopAndRemoveInstancesFor(Domain domain) {
        List<DomainMessageListenerContainer> items = getInstancesByDomain(domain);
        stop(items);
        items.forEach(item -> {
            instances.remove(item);
        });
    }

    private void shutdownInstance(DomainMessageListenerContainer instance) {
        try {
            LOG.info("Shutting down MessageListenerContainer instance: {}", instance);
            instance.shutdown();
        } catch (Exception e) {
            LOG.error("Error while shutting down MessageListenerContainer", e);
        }
    }

    private void stopInstance(DomainMessageListenerContainer instance) {
        try {
            LOG.info("Stopping MessageListenerContainer instance: {}", instance);
            instance.get().stop();
        } catch (Exception e) {
            LOG.error("Error while stopping MessageListenerContainer", e);
        }
    }

}
