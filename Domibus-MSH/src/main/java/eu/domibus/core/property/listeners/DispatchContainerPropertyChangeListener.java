package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.configuration.storage.Storage;
import eu.domibus.messaging.MessageListenerContainerInitializer;
import eu.domibus.property.DomibusPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class DispatchContainerPropertyChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, "domibus.dispatcher.concurency");
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        MessageListenerContainerInitializer messageListenerContainerInitializer = applicationContext.getBean(MessageListenerContainerInitializer.class);

        final Domain domain = domainService.getDomain(domainCode);

        messageListenerContainerInitializer.createSendMessageListenerContainer(domain);
    }
}
