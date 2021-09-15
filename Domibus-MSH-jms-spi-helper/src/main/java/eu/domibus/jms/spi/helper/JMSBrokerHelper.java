package eu.domibus.jms.spi.helper;

import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.jms.core.JmsOperations;
import org.springframework.stereotype.Service;

@Service
public class JMSBrokerHelper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(JMSBrokerHelper.class);

    protected DomibusPropertyProvider domibusPropertyProvider;

    public JMSBrokerHelper(DomibusPropertyProvider domibusPropertyProvider) {
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    public void isJMSBrokerAlive(JmsOperations jmsSender) {
        LOG.debug("Checking if broker is alive");
        final String pullQueue = domibusPropertyProvider.getProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_JMS_QUEUE_PULL);

        LOG.debug("Browsing queue [{}]", pullQueue);
        jmsSender.browse(pullQueue, (session, browser) -> null);
    }
}
