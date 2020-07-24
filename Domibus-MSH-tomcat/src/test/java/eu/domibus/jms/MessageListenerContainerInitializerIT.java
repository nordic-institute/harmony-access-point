package eu.domibus.jms;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import eu.domibus.AbstractIT;
import eu.domibus.core.jms.MessageListenerContainerInitializer;
import eu.domibus.core.property.listeners.ConcurrencyChangeListener;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DISPATCHER_CONCURENCY;

/**
 * @author Sebastian-Ion TINCU
 * @since 4.2
 */
@Configuration
public class MessageListenerContainerInitializerIT extends AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageListenerContainerInitializerIT.class);

    @Autowired
    private ConcurrencyChangeListener concurrencyChangeListener;

    @Autowired
    private TransactionExceptionBrokerPlugin transactionExceptionBrokerPlugin;

    @Autowired
    private MessageListenerContainerInitializer messageListenerContainerInitializer;

    @Test
    public void testSetListenerConcurrency() {
        for (int i = 1; i <= 30; i++) {
            LOG.info("Reconfiguring the send message listener container for the [{}] time", i);

            // WHEN
            concurrencyChangeListener.propertyValueChanged(DomainDTO.DEFAULT_DOMAIN.getCode(), DOMIBUS_DISPATCHER_CONCURENCY, i + "-30");

            // THEN
            Assert.assertFalse("Should have not thrown an XAException stating that the 2 phase commit cannot be "
                                       + "completed because the XA transaction has not yet been prepared",
                               transactionExceptionBrokerPlugin.isTestFailed());
        }
    }

    @Test
    public void testStopListeners() {
        // WHEN
        messageListenerContainerInitializer.destroy();

        // THEN
        Assert.assertFalse("Should have not thrown an XAException stating that the 2 phase commit cannot be "
                                   + "completed because the XA transaction has not yet been prepared",
                           transactionExceptionBrokerPlugin.isTestFailed());
    }
}
