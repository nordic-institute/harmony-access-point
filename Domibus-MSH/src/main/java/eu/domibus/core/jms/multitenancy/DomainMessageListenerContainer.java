package eu.domibus.core.jms.multitenancy;

import eu.domibus.api.multitenancy.Domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * Domibus specific MessageListenerContainer, created for a specific domain;
 * We need this because we manage them explicitly and we need also to replace one at runtime
 */
public class DomainMessageListenerContainer extends DefaultMessageListenerContainer {
    private Domain domain;

    public DomainMessageListenerContainer(Domain domain) {
        this.domain = domain;
    }

    /**
     * Method used to identify the bean by name
     * @return the name of the bean
     */

    public String getName() {
        return super.getBeanName();
    }

    /**
     * Gets the domain on which the container was created
     * @return the domain
     */
    public Domain getDomain() { return this.domain; }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", getName())
                .append("domain", domain)
                .toString();
    }
}
