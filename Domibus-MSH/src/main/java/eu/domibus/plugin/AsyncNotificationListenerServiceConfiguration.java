package eu.domibus.plugin;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.config.JmsListenerContainerFactory;

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
    protected PluginEventNotifierProvider pluginEventNotifierProvider;
    protected ObjectProvider<AsyncNotificationListenerService> myPrototypeProvider;


    public AsyncNotificationListenerServiceConfiguration(@Qualifier("internalJmsListenerContainerFactory") JmsListenerContainerFactory jmsListenerContainerFactory,
                                                         AuthUtils authUtils,
                                                         DomainContextProvider domainContextProvider,
                                                         PluginEventNotifierProvider pluginEventNotifierProvider,
                                                         ObjectProvider<AsyncNotificationListenerService> myPrototypeProvider,
                                                         @Autowired(required = false) List<NotificationListenerService> notificationListenerServices) {
        this.jmsListenerContainerFactory = jmsListenerContainerFactory;
        this.authUtils = authUtils;
        this.domainContextProvider = domainContextProvider;
        this.pluginEventNotifierProvider = pluginEventNotifierProvider;
        this.myPrototypeProvider = myPrototypeProvider;
        this.notificationListenerServices = notificationListenerServices;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public AsyncNotificationListenerService createAsyncNotificationListenerService(NotificationListenerService notificationListenerService) {
        AsyncNotificationListenerService notificationListenerServiceImpl = new AsyncNotificationListenerService(domainContextProvider, notificationListenerService, pluginEventNotifierProvider, authUtils);
        return notificationListenerServiceImpl;
    }
}
