package eu.domibus.plugin.ws.backend.reliability.queue;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.environment.ApplicationServerCondition;
import eu.domibus.plugin.ws.property.WSPluginPropertyManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.jms.Queue;
import java.io.IOException;

import static eu.domibus.plugin.ws.backend.reliability.queue.WSMessageListenerContainerConfiguration.WS_PLUGIN_SEND_QUEUE;

/**
 * Class responsible for the configuration of the plugin for an application server, WebLogic and WildFly
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
@Conditional(ApplicationServerCondition.class)
@Configuration
public class WSBackendApplicationServerConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSBackendApplicationServerConfiguration.class);

 /*   @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;*/

    @Bean(WS_PLUGIN_SEND_QUEUE)
    public JndiObjectFactoryBean sendMessageQueue(WSPluginPropertyManager wsPluginPropertyManager) throws IOException {
        // return propertyRetrieveManager.getInternalProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_CONFIG_LOCATION);
        String queueName = wsPluginPropertyManager.getKnownPropertyValue(WSPluginPropertyManager.DISPATCHER_SEND_QUEUE_NAME);
      /*  if(queueName ==null)
        {
            LOG.debug("WSPluginPropertyManager.DISPATCHER_SEND_QUEUE_NAME [{}]", WSPluginPropertyManager.DISPATCHER_SEND_QUEUE_NAME);
            List<Resource> resources = new ArrayList<>();
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] pluginDefaultResourceList = resolver.getResources("classpath*:config/ws-plugin-default.properties");
            LOG.debug("Adding the following plugin default properties files [{}]", pluginDefaultResourceList);
            resources.addAll(Arrays.asList(pluginDefaultResourceList));

            String queueName1 =  domibusPropertyProvider.getProperty(WSPluginPropertyManager.DISPATCHER_SEND_QUEUE_NAME);
            LOG.debug("Using ws plugin send queue name1 [{}]", queueName1);
        }*/
        LOG.debug("Using ws plugin send queue name [{}]", queueName);

        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();

        jndiObjectFactoryBean.setJndiName(queueName);

        jndiObjectFactoryBean.setExpectedType(Queue.class);
        return jndiObjectFactoryBean;
    }

}
