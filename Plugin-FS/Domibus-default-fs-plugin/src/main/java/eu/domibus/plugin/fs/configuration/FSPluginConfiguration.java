package eu.domibus.plugin.fs.configuration;

import eu.domibus.common.MessageStatus;
import eu.domibus.ext.services.DomainTaskExtExecutor;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.DomibusEnvironmentUtil;
import eu.domibus.plugin.fs.*;
import eu.domibus.plugin.fs.ebms3.ObjectFactory;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import eu.domibus.plugin.fs.queue.FSSendMessageListenerContainer;
import eu.domibus.plugin.fs.worker.FSDomainService;
import eu.domibus.plugin.fs.worker.FSProcessFileService;
import eu.domibus.plugin.fs.worker.FSSendMessagesService;
import eu.domibus.plugin.notification.PluginAsyncNotificationConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.jms.Queue;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.LinkedList;
import java.util.List;

import static eu.domibus.plugin.fs.FSPluginImpl.PLUGIN_NAME;

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

    @Bean(PLUGIN_NAME)
    public FSPluginImpl createFSPlugin(FSMessageTransformer defaultTransformer, FSFilesManager fsFilesManager, FSPluginProperties fsPluginProperties,
                                       FSSendMessagesService fsSendMessagesService, FSProcessFileService fsProcessFileService,
                                       DomainTaskExtExecutor domainTaskExtExecutor, FSDomainService fsDomainService, FSXMLHelper fsxmlHelper,
                                       FSMimeTypeHelper fsMimeTypeHelper, FSFileNameHelper fsFileNameHelper,
                                       FSSendMessageListenerContainer fsSendMessageListenerContainer, DomibusPropertyExtService domibusPropertyExtService) {
        FSPluginImpl fsPlugin = new FSPluginImpl(defaultTransformer, fsFilesManager, fsPluginProperties, fsSendMessagesService, fsProcessFileService, domainTaskExtExecutor,
                fsDomainService, fsxmlHelper, fsMimeTypeHelper, fsFileNameHelper, fsSendMessageListenerContainer, domibusPropertyExtService);
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
