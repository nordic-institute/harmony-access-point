package eu.domibus.core.jms;

import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.ConnectionFactory;
import javax.jms.Session;
import java.util.Optional;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class InternalJmsListenerContainerFactoryConfigurationTest {

    @Tested
    InternalJmsListenerContainerFactoryConfiguration internalJmsListenerContainerFactoryConfiguration;

    @Mocked
    DefaultJmsListenerContainerFactory defaultJmsListenerContainerFactory;

    @Test
    public void internalJmsListenerContainerFactory(@Injectable ConnectionFactory connectionFactory,
                                                 @Injectable PlatformTransactionManager transactionManager,
                                                 @Injectable DomibusPropertyProvider domibusPropertyProvider,
                                                 @Injectable MappingJackson2MessageConverter jackson2MessageConverter,
                                                 @Injectable Optional<JndiDestinationResolver> internalDestinationResolver,
                                                @Injectable SchedulingTaskExecutor schedulingTaskExecutor) {

        String concurrency = "2-3";

        new Expectations() {{
            domibusPropertyProvider.getProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_INTERNAL_QUEUE_CONCURENCY);
            this.result = concurrency;
        }};


        internalJmsListenerContainerFactoryConfiguration.internalJmsListenerContainerFactory(connectionFactory, transactionManager, domibusPropertyProvider, internalDestinationResolver,schedulingTaskExecutor);

        new Verifications() {{
            MessageConverter messageConverter = null;
            defaultJmsListenerContainerFactory.setMessageConverter(messageConverter = withCapture());
            Assert.assertEquals(messageConverter, jackson2MessageConverter);

            ConnectionFactory cf = null;
            defaultJmsListenerContainerFactory.setConnectionFactory(cf = withCapture());
            Assert.assertEquals(connectionFactory, cf);

            PlatformTransactionManager tm = null;
            defaultJmsListenerContainerFactory.setTransactionManager(tm = withCapture());
            Assert.assertEquals(transactionManager, tm);

            String factoryConcurrency = null;
            defaultJmsListenerContainerFactory.setConcurrency(factoryConcurrency = withCapture());
            Assert.assertEquals(factoryConcurrency, concurrency);

            defaultJmsListenerContainerFactory.setSessionTransacted(true);

            Integer sessionAckMode = null;
            defaultJmsListenerContainerFactory.setSessionAcknowledgeMode(sessionAckMode = withCapture());
            Assert.assertEquals(Session.SESSION_TRANSACTED, sessionAckMode.intValue());
        }};
    }
}