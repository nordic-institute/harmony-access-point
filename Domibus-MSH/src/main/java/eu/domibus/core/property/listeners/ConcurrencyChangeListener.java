package eu.domibus.core.property.listeners;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.core.jms.MessageListenerContainerInitializer;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DISPATCHER_CONCURENCY;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DISPATCHER_LARGE_FILES_CONCURRENCY;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DISPATCHER_SPLIT_AND_JOIN_CONCURRENCY;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PULL_QUEUE_CONCURENCY;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PULL_RECEIPT_QUEUE_CONCURRENCY;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_RETENTION_JMS_CONCURRENCY;
import static eu.domibus.core.jms.multitenancy.MessageListenerContainerConfiguration.DISPATCH_CONTAINER;
import static eu.domibus.core.jms.multitenancy.MessageListenerContainerConfiguration.PULL_MESSAGE_CONTAINER;
import static eu.domibus.core.jms.multitenancy.MessageListenerContainerConfiguration.PULL_RECEIPT_CONTAINER;
import static eu.domibus.core.jms.multitenancy.MessageListenerContainerConfiguration.RETENTION_CONTAINER;
import static eu.domibus.core.jms.multitenancy.MessageListenerContainerConfiguration.SEND_LARGE_MESSAGE_CONTAINER;
import static eu.domibus.core.jms.multitenancy.MessageListenerContainerConfiguration.SPLIT_AND_JOIN_CONTAINER;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of concurrency properties
 */
@Service
public class ConcurrencyChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    private DomainService domainService;

    @Autowired
    private MessageListenerContainerInitializer messageListenerContainerInitializer;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsAnyIgnoreCase(propertyName,
                DOMIBUS_DISPATCHER_CONCURENCY,
                DOMIBUS_DISPATCHER_LARGE_FILES_CONCURRENCY,
                DOMIBUS_RETENTION_JMS_CONCURRENCY,
                DOMIBUS_DISPATCHER_SPLIT_AND_JOIN_CONCURRENCY,
                DOMIBUS_PULL_RECEIPT_QUEUE_CONCURRENCY,
                DOMIBUS_PULL_QUEUE_CONCURENCY);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        final Domain domain = domainService.getDomain(domainCode);

        switch (propertyName) {
            case DOMIBUS_DISPATCHER_CONCURENCY:
                messageListenerContainerInitializer.setConcurrency(domain, DISPATCH_CONTAINER, propertyValue);
                break;
            case DOMIBUS_DISPATCHER_LARGE_FILES_CONCURRENCY:
                messageListenerContainerInitializer.setConcurrency(domain, SEND_LARGE_MESSAGE_CONTAINER, propertyValue);
                break;
            case DOMIBUS_RETENTION_JMS_CONCURRENCY:
                messageListenerContainerInitializer.setConcurrency(domain, RETENTION_CONTAINER, propertyValue);
                break;
            case DOMIBUS_DISPATCHER_SPLIT_AND_JOIN_CONCURRENCY:
                messageListenerContainerInitializer.setConcurrency(domain, SPLIT_AND_JOIN_CONTAINER, propertyValue);
                break;
            case DOMIBUS_PULL_RECEIPT_QUEUE_CONCURRENCY:
                messageListenerContainerInitializer.setConcurrency(domain, PULL_RECEIPT_CONTAINER, propertyValue);
                break;
            case DOMIBUS_PULL_QUEUE_CONCURENCY:
                messageListenerContainerInitializer.setConcurrency(domain, PULL_MESSAGE_CONTAINER, propertyValue);
                break;
            default:
                throw new IllegalArgumentException("Unknown property: " + propertyName);
        }
    }
}
