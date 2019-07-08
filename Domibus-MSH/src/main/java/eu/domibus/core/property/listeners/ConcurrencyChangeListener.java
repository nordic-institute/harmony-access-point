package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.messaging.MessageListenerContainerInitializer;
import eu.domibus.property.DomibusPropertyChangeListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class ConcurrencyChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected ApplicationContext applicationContext;

    private String[] handledProperties = new String[]{
            "domibus.dispatcher.concurency",
            "domibus.dispatcher.largeFiles.concurrency",
            "domibus.retention.jms.concurrency",
            "domibus.dispatcher.splitAndJoin.concurrency",
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
