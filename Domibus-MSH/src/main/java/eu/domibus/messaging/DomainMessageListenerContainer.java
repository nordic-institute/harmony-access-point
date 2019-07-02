package eu.domibus.messaging;

import eu.domibus.api.multitenancy.Domain;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

public class DomainMessageListenerContainer extends DefaultMessageListenerContainer {
    private Domain domain;

    public DomainMessageListenerContainer(Domain domain) {
        this.domain = domain;
    }

    public String getName() {
        return super.getBeanName();
    }

    public Domain getDomain() { return this.domain; }
}
