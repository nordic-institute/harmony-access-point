package eu.domibus.plugin;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.config.JmsListenerContainerFactory;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class AsyncNotificationListenerServiceConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AsyncNotificationListenerServiceConfiguration.class);

    protected List<NotificationListenerService> notificationListenerServices;
    protected JmsListenerContainerFactory jmsListenerContainerFactory;
    protected AuthUtils authUtils;
    protected DomainContextProvider domainContextProvider;
    protected NotificationListenerService notificationListenerService;
    protected PluginEventNotifierProvider pluginEventNotifierProvider;

    public AsyncNotificationListenerServiceConfiguration(@Autowired(required = false) List<NotificationListenerService> notificationListenerServices,
                                                         @Qualifier("internalJmsListenerContainerFactory") JmsListenerContainerFactory jmsListenerContainerFactory,
                                                         AuthUtils authUtils,
                                                         DomainContextProvider domainContextProvider,
                                                         NotificationListenerService notificationListenerService,
                                                         PluginEventNotifierProvider pluginEventNotifierProvider) {
        this.notificationListenerServices = notificationListenerServices;
        this.jmsListenerContainerFactory = jmsListenerContainerFactory;
        this.authUtils = authUtils;
        this.domainContextProvider = domainContextProvider;
        this.notificationListenerService = notificationListenerService;
        this.pluginEventNotifierProvider = pluginEventNotifierProvider;
    }

    @PostConstruct
    public void init() {
        LOG.info("Initializing services of type AsyncNotificationListenerService");

        for (NotificationListenerService notificationListenerService : notificationListenerServices) {
            initializeAsyncNotificationListerService(notificationListenerService);
        }
    }

    protected void initializeAsyncNotificationListerService(NotificationListenerService notificationListenerService) {
        if (notificationListenerService.getMode() == BackendConnector.Mode.PULL) {
            LOG.info("No async notification listener is created for plugin [{}]: plugin type is PULL", notificationListenerService.getBackendName());
            return;
        }
        if (notificationListenerService.getBackendNotificationQueue() == null) {
            LOG.info("No notification queue configured for plugin [{}]. No async notification listener is created", notificationListenerService.getBackendName());
            return;
        }

        notificationListenerServiceImpl(notificationListenerService);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public AsyncNotificationListenerService notificationListenerServiceImpl(NotificationListenerService notificationListenerService) {
        LOG.info("Instantiating AsyncNotificationListenerService for backend [{}]", notificationListenerService.getBackendName());

        AsyncNotificationListenerService notificationListenerServiceImpl = new AsyncNotificationListenerService(jmsListenerContainerFactory, authUtils, domainContextProvider, notificationListenerService, pluginEventNotifierProvider);
        return notificationListenerServiceImpl;
    }
}
