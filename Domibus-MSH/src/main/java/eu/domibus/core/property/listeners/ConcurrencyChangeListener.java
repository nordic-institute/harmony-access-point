package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.messaging.MessageListenerContainerInitializer;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.*;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 *
 * Handles the change of concurrency properties
 */
@Service
public class ConcurrencyChangeListener implements PluginPropertyChangeListener {

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected ApplicationContext applicationContext;

    private String[] handledProperties = new String[]{
            DOMIBUS_DISPATCHER_CONCURENCY,
            DOMIBUS_DISPATCHER_LARGE_FILES_CONCURRENCY,
            DOMIBUS_RETENTION_JMS_CONCURRENCY,
            DOMIBUS_DISPATCHER_SPLIT_AND_JOIN_CONCURRENCY,
    };

    @Override
    public boolean handlesProperty(String propertyName) {
        return Arrays.stream(handledProperties).anyMatch(p -> p.equalsIgnoreCase(propertyName));
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        MessageListenerContainerInitializer messageListenerContainerInitializer = applicationContext.getBean(MessageListenerContainerInitializer.class);

        final Domain domain = domainService.getDomain(domainCode);

        switch (propertyName) {
            case DOMIBUS_DISPATCHER_CONCURENCY:
                messageListenerContainerInitializer.createSendMessageListenerContainer(domain);
                break;
            case DOMIBUS_DISPATCHER_LARGE_FILES_CONCURRENCY:
                messageListenerContainerInitializer.createSendLargeMessageListenerContainer(domain);
                break;
            case DOMIBUS_RETENTION_JMS_CONCURRENCY:
                messageListenerContainerInitializer.createRetentionListenerContainer(domain);
                break;
            case DOMIBUS_DISPATCHER_SPLIT_AND_JOIN_CONCURRENCY:
                messageListenerContainerInitializer.createSplitAndJoinListenerContainer(domain);
                break;
        }
    }
}
