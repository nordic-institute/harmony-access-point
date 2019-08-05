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

    @Autowired
    MessageListenerContainerInitializer messageListenerContainerInitializer;

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

        final Domain domain = domainService.getDomain(domainCode);

        switch (propertyName) {
            case "domibus.dispatcher.concurency":
                messageListenerContainerInitializer.createSendMessageListenerContainer(domain);
                break;
            case "domibus.dispatcher.largeFiles.concurrency":
                messageListenerContainerInitializer.createSendLargeMessageListenerContainer(domain);
                break;
            case "domibus.retention.jms.concurrency":
                messageListenerContainerInitializer.createRetentionListenerContainer(domain);
                break;
            case "domibus.dispatcher.splitAndJoin.concurrency":
                messageListenerContainerInitializer.createSplitAndJoinListenerContainer(domain);
                break;
        }
    }
}
