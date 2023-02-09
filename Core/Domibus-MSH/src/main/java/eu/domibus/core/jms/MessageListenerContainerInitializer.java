package eu.domibus.core.jms;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainMessageListenerContainer;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainsAware;
import eu.domibus.api.plugin.BackendConnectorService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.jms.multitenancy.DomainMessageListenerContainerFactory;
import eu.domibus.core.jms.multitenancy.DomainMessageListenerContainerImpl;
import eu.domibus.core.jms.multitenancy.MessageListenerContainerConfiguration;
import eu.domibus.core.jms.multitenancy.PluginDomainMessageListenerContainerAdapter;
import eu.domibus.core.message.UserMessagePriorityConfiguration;
import eu.domibus.core.message.UserMessagePriorityService;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.PluginMessageListenerContainer;
import eu.domibus.plugin.EnableAware;
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
import java.util.Optional;
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

    protected final BackendConnectorService backendConnectorService;

    protected final BackendConnectorProvider backendConnectorProvider;

    public MessageListenerContainerInitializer(ApplicationContext applicationContext, DomainMessageListenerContainerFactory messageListenerContainerFactory,
                                               DomibusPropertyProvider domibusPropertyProvider, DomainService domainService,
                                               DomibusCoreMapper coreMapper, UserMessagePriorityService userMessagePriorityService,
                                               BackendConnectorService backendConnectorService, BackendConnectorProvider backendConnectorProvider) {
        this.applicationContext = applicationContext;
        this.messageListenerContainerFactory = messageListenerContainerFactory;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domainService = domainService;
        this.coreMapper = coreMapper;
        this.userMessagePriorityService = userMessagePriorityService;
        this.backendConnectorService = backendConnectorService;
        this.backendConnectorProvider = backendConnectorProvider;
    }

    public void initialize() {
        final List<Domain> domains = domainService.getDomains();
        createInstancesFor(domains);
    }

    @PreDestroy
    public void destroy() {
        stop(instances);
    }

    @Override
    public void onDomainAdded(final Domain domain) {
        createInstancesFor(domain);
    }

    @Override
    public void onDomainRemoved(Domain domain) {
        List<DomainMessageListenerContainer> items = getInstancesByDomain(domain);
        stopAndRemoveInstancesFor(items);
    }

    public void createMessageListenersForPlugin(String backendName, Domain domain) {
        if (backendName == null) {
            LOG.info("Backend name provided is null, exiting.");
            return;
        }

        if (pluginMessageListenerContainerAlreadyExists(backendName, domain)) {
            LOG.info("Message listener container for plugin [{}] and domain [{}] already exists; exiting.", backendName, domain);
            return;
        }

        EnableAware backend = backendConnectorProvider.getEnableAware(backendName);
        if (backend == null) {
            LOG.info("Could not find backend named [{}], exiting.", backendName);
            return;
        }

        final Optional<Map.Entry<String, PluginMessageListenerContainer>> entry = applicationContext.getBeansOfType(PluginMessageListenerContainer.class).entrySet()
                .stream().filter(el -> el.getValue().equals(backend.getMessageListenerContainerFactory())).findFirst();
        if (!entry.isPresent()) {
            LOG.warn("Could not find plugin message listener container for [{}] on domain [{}]; exiting", backendName, domain);
            return;
        }

        createMessageListenerContainerFor(backendName, domain, entry.get().getKey(), entry.get().getValue());
    }

    public void destroyMessageListenersForPlugin(String backendName, Domain domain) {
        List<DomainMessageListenerContainer> items = getPluginInstancesByBackendAndDomain(backendName, domain);
        stopAndRemoveInstancesFor(items);
    }

    protected void createSendMessageListenerContainer(Domain domain) {
        LOG.info("Creating the SendMessageListenerContainer for domain [{}] ", domain);

        DomainMessageListenerContainerImpl instance = messageListenerContainerFactory.createSendMessageListenerContainer(domain, null, DOMIBUS_DISPATCHER_CONCURENCY);
        instance.start();
        instances.add(instance);
        LOG.info("MessageListenerContainer initialized for domain [{}]", domain);
    }

    private boolean pluginMessageListenerContainerAlreadyExists(String backendName, Domain domain) {
        return getPluginInstancesByBackendAndDomain(backendName, domain).size() > 0;
    }

    public void setConcurrency(Domain domain, String beanName, String concurrency) {
        DomainMessageListenerContainer instance = getInstanceByNameAndDomain(domain, beanName);
        if (instance != null) {
            instance.setConcurrency(concurrency);
        }
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

    /**
     * It will collect and instantiate all {@link PluginMessageListenerContainer} defined in plugins
     *
     * @param domain
     */
    protected void createMessageListenersForPlugins(Domain domain) {
        final Map<String, PluginMessageListenerContainer> beansOfType = applicationContext.getBeansOfType(PluginMessageListenerContainer.class);
        List<EnableAware> plugins = backendConnectorProvider.getEnableAwares();

        // there can be older plugins which do not have message listener container declared on the backend connector
        // so we must find them lik this
        for (Map.Entry<String, PluginMessageListenerContainer> entry : beansOfType.entrySet()) {
            PluginMessageListenerContainer pluginMLCFactory = entry.getValue();
            String beanName = entry.getKey();
            // if it is a new plugin, we can get the message listener container on the backend connector interface itself
            // and we get the name this way
            Optional<EnableAware> found = plugins.stream().filter(plugin -> plugin.getMessageListenerContainerFactory() == pluginMLCFactory).findFirst();
            String pluginName = null;
            if (found.isPresent()) {
                pluginName = found.get().getName();
                LOG.debug("Could find the plugin name [{}] for plugin message listener container factory [{}], so it is 5.1 version or higher.",
                        pluginName, beanName);
            } else {
                LOG.debug("Could not find the plugin name for plugin message listener container factory [{}], so it is 5.0 version or lower.",
                        beanName);
            }
            createMessageListenerContainerFor(pluginName, domain, beanName, pluginMLCFactory);
        }
    }

    private void createMessageListenerContainerFor(String pluginName, Domain domain, String beanName, PluginMessageListenerContainer containerFactory) {
        if (StringUtils.isNotBlank(pluginName) && backendConnectorService.shouldCoreManageResources(pluginName)) {
            if (!backendConnectorService.isBackendConnectorEnabled(pluginName, domain.getCode())) {
                LOG.info("Message listener container for plugin [{}] and domain [{}] is not enabled so exiting.", pluginName, domain);
                return;
            }
        } // else pluginName is null for old plugins - backwards compatibility

        LOG.debug("Creating message listener container for plugin [{}] and domain [{}] ", pluginName, domain);

        DomainDTO domainDTO = coreMapper.domainToDomainDTO(domain);
        MessageListenerContainer instance = containerFactory.createMessageListenerContainer(domainDTO);
        if (instance == null) {
            LOG.info("Message listener container [{}] for domain [{}] returned null so exiting.", beanName, domain);
            return;
        }

        DomainMessageListenerContainer adapter = new PluginDomainMessageListenerContainerAdapter(instance, domain, beanName, pluginName);
        instance.start();
        instances.add(adapter);
        LOG.info("{} initialized for domain [{}]", beanName, domain);
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

    private List<DomainMessageListenerContainer> getPluginInstancesByBackendAndDomain(String backendName, Domain domain) {
        return instances.stream()
                .filter(instance -> instance instanceof PluginDomainMessageListenerContainerAdapter)
                .map(instance -> (PluginDomainMessageListenerContainerAdapter) instance)
                .filter(instance -> instance.getDomain().equals(domain))
                .filter(instance -> StringUtils.equals(instance.getPluginName(), backendName))
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

    private void stopAndRemoveInstancesFor(List<DomainMessageListenerContainer> items) {
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
