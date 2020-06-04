package eu.domibus.core.clustering;

import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.ConnectionFactory;
import javax.jms.Topic;
import java.util.Optional;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class ControllerListenerConfigurationTest {

    @Tested
    ControllerListenerConfiguration controllerListenerConfiguration;

    @Test
    public void createDefaultMessageListenerContainer(@Injectable ConnectionFactory connectionFactory,
                                                      @Injectable Topic destination,
                                                      @Injectable ControllerListenerService messageListener,
                                                      @Injectable PlatformTransactionManager transactionManager,
                                                      @Injectable Optional<JndiDestinationResolver> internalDestinationResolver,
                                                      @Injectable DomibusPropertyProvider domibusPropertyProvider) {
        new Expectations() {{
            domibusPropertyProvider.getProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_JMS_INTERNAL_COMMAND_CONCURENCY);
            result = "2-3";
        }};

        DefaultMessageListenerContainer defaultMessageListenerContainer = controllerListenerConfiguration.createDefaultMessageListenerContainer(connectionFactory, destination, messageListener, transactionManager, internalDestinationResolver, domibusPropertyProvider);
        Assert.assertEquals(2, defaultMessageListenerContainer.getConcurrentConsumers());
        Assert.assertEquals(3, defaultMessageListenerContainer.getMaxConcurrentConsumers());
        Assert.assertEquals(destination, defaultMessageListenerContainer.getDestination());

    }
}