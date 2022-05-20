package eu.domibus.core.plugin.notification;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.notification.AsyncNotificationConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.config.JmsListenerContainerFactory;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class PluginAsyncNotificationListenerConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginAsyncNotificationListenerConfiguration.class);

    protected JmsListenerContainerFactory jmsListenerContainerFactory;
    protected AuthUtils authUtils;
    protected DomainContextProvider domainContextProvider;
    protected PluginEventNotifierProvider pluginEventNotifierProvider;

    public PluginAsyncNotificationListenerConfiguration(@Qualifier("internalJmsListenerContainerFactory") JmsListenerContainerFactory jmsListenerContainerFactory,
                                                        AuthUtils authUtils,
                                                        DomainContextProvider domainContextProvider,
                                                        PluginEventNotifierProvider pluginEventNotifierProvider) {
        this.jmsListenerContainerFactory = jmsListenerContainerFactory;
        this.authUtils = authUtils;
        this.domainContextProvider = domainContextProvider;
        this.pluginEventNotifierProvider = pluginEventNotifierProvider;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public PluginAsyncNotificationListener createAsyncNotificationListener(AsyncNotificationConfiguration asyncNotificationConfiguration) {
        PluginAsyncNotificationListener notificationListenerServiceImpl = new PluginAsyncNotificationListener(domainContextProvider, asyncNotificationConfiguration, pluginEventNotifierProvider, authUtils);
        return notificationListenerServiceImpl;
    }
}
