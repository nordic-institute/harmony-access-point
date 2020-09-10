package eu.domibus.plugin.fs.configuration;

import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.DomibusEnvironmentUtil;
import eu.domibus.plugin.fs.BackendFSImpl;
import eu.domibus.plugin.notification.PluginAsyncNotificationConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.jms.Queue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for the configuration of the plugin, independent of any server
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class FSPluginConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSPluginConfiguration.class);

    public static final String NOTIFY_BACKEND_QUEUE_JNDI = "jms/domibus.notification.filesystem";

    @Bean("backendFSPlugin")
    public BackendFSImpl createFSPlugin() {
        return new BackendFSImpl();
    }

    @Bean("fsPluginAsyncPluginConfiguration")
    public PluginAsyncNotificationConfiguration pluginAsyncNotificationConfiguration(@Qualifier("notifyBackendFSQueue") Queue notifyBackendFSQueue,
                                                                                     BackendFSImpl backendFS,
                                                                                     Environment environment) {
        PluginAsyncNotificationConfiguration pluginAsyncNotificationConfiguration = new PluginAsyncNotificationConfiguration(backendFS, notifyBackendFSQueue);
        if (DomibusEnvironmentUtil.isApplicationServer(environment)) {
            String queueNotificationJndi = NOTIFY_BACKEND_QUEUE_JNDI;
            LOG.debug("Domibus is running inside an application server. Setting the queue name to [{}]", queueNotificationJndi);
            pluginAsyncNotificationConfiguration.setQueueName(queueNotificationJndi);
        }
        return pluginAsyncNotificationConfiguration;
    }

    @Bean("fsPluginProperties")
    public PropertiesFactoryBean fsPluginProperties(DomibusConfigurationExtService domibusConfigurationExtService) throws IOException {
        PropertiesFactoryBean result = new PropertiesFactoryBean();
        result.setIgnoreResourceNotFound(true);

        List<Resource> resources = new ArrayList<>();
        resources.add(new ClassPathResource("config/fs-plugin.properties"));

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        String domibusConfigLocation = domibusConfigurationExtService.getConfigLocation();
        String location = "file:///" + domibusConfigLocation + "/plugins/config/fs-plugin.properties";
        LOG.debug("Resolving resource [{}]", location);
        Resource domibusProperties = resolver.getResource(location);
        resources.add(domibusProperties);

        result.setLocations(resources.toArray(new Resource[0]));
        return result;
    }

}
