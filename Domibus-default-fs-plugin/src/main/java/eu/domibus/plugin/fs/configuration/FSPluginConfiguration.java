package eu.domibus.plugin.fs.configuration;

import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationType;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.DomibusEnvironmentUtil;
import eu.domibus.plugin.fs.*;
import eu.domibus.plugin.fs.ebms3.ObjectFactory;
import eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl;
import eu.domibus.plugin.notification.PluginAsyncNotificationConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.jms.Queue;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
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
    public static final String NOTIFY_BACKEND_FS_QUEUE_NAME = "notifyBackendFSQueue";

    @Value("file:///${domibus.config.location}/plugins/config/fs-plugin.properties")
    protected String fsPluginExternalPropertiesFile;

    protected List<NotificationType> defaultMessageNotifications = Arrays.asList(
                            NotificationType.MESSAGE_RECEIVED, NotificationType.MESSAGE_SEND_FAILURE, NotificationType.MESSAGE_RECEIVED_FAILURE,
                            NotificationType.MESSAGE_SEND_SUCCESS, NotificationType.MESSAGE_STATUS_CHANGE);

    @Bean("backendFSPlugin")
    public FSPluginImpl createFSPlugin(DomibusPropertyExtService domibusPropertyExtService) {
        List<NotificationType> messageNotifications = domibusPropertyExtService.getConfiguredNotifications(FSPluginPropertiesMetadataManagerImpl.PROPERTY_PREFIX + FSPluginPropertiesMetadataManagerImpl.MESSAGE_NOTIFICATIONS);
        LOG.debug("Using the following message notifications [{}]", messageNotifications);
        if (!messageNotifications.containsAll(defaultMessageNotifications)) {
            LOG.warn("FSPlugin will not function properly if the following message notifications will not be set: [{}]",
                    defaultMessageNotifications);
        }

        FSPluginImpl fsPlugin = new FSPluginImpl();
        fsPlugin.setRequiredNotifications(messageNotifications);
        return fsPlugin;
    }

    @Bean("fsPluginAsyncPluginConfiguration")
    public PluginAsyncNotificationConfiguration pluginAsyncNotificationConfiguration(@Qualifier(NOTIFY_BACKEND_FS_QUEUE_NAME) Queue notifyBackendFSQueue,
                                                                                     FSPluginImpl backendFS,
                                                                                     Environment environment) {
        PluginAsyncNotificationConfiguration pluginAsyncNotificationConfiguration = new PluginAsyncNotificationConfiguration(backendFS, notifyBackendFSQueue);
        if (DomibusEnvironmentUtil.INSTANCE.isApplicationServer(environment)) {
            String queueNotificationJndi = NOTIFY_BACKEND_QUEUE_JNDI;
            LOG.debug("Domibus is running inside an application server. Setting the queue name to [{}]", queueNotificationJndi);
            pluginAsyncNotificationConfiguration.setQueueName(queueNotificationJndi);
        }
        return pluginAsyncNotificationConfiguration;
    }

    @Bean("fsPluginProperties")
    public PropertiesFactoryBean fsPluginProperties() throws IOException {
        PropertiesFactoryBean result = new PropertiesFactoryBean();
        result.setIgnoreResourceNotFound(true);

        List<Resource> resources = new ArrayList<>();
        resources.add(new ClassPathResource("config/fs-plugin.properties"));

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        LOG.debug("Using FSPlugin external properties file [{}]", fsPluginExternalPropertiesFile);
        Resource domibusProperties = resolver.getResource(fsPluginExternalPropertiesFile);
        resources.add(domibusProperties);

        result.setLocations(resources.toArray(new Resource[0]));
        return result;
    }

    @Bean("fsPluginJaxbContext")
    public JAXBContext jaxbContext() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        return jaxbContext;
    }

    @Bean
    public FSXMLHelper fsxmlHelper(@Qualifier("fsPluginJaxbContext") JAXBContext jaxbContext) {
        FSXMLHelperImpl result = new FSXMLHelperImpl(jaxbContext);
        return result;
    }

    @Bean
    public FSMimeTypeHelper fsMimeTypeHelper() {
        FSMimeTypeHelperImpl result = new FSMimeTypeHelperImpl();
        return result;
    }

    @Bean
    public FSFileNameHelper fsFileNameHelper() {
        List<String> stateSuffixes = getStateSuffixes();
        LOG.debug("Using state suffixes [{}]", stateSuffixes);
        FSFileNameHelper result = new FSFileNameHelper(stateSuffixes);
        return result;
    }

    public List<String> getStateSuffixes() {
        List<String> result = new LinkedList<>();
        for (MessageStatus status : MessageStatus.values()) {
            result.add(FSFileNameHelper.EXTENSION_SEPARATOR + status.name());
        }
        return result;
    }

}
