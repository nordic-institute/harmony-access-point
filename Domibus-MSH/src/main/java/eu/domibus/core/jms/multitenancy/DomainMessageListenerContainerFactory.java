package eu.domibus.core.jms.multitenancy;

import eu.domibus.api.multitenancy.Domain;

/**
 * @author Ion Perpegel
 * @since 4.0
 *
 * Interface for creating Message Listener containers for a specified domain
 */
public interface DomainMessageListenerContainerFactory {

    DomainMessageListenerContainer createSendMessageListenerContainer(Domain domain, String selector, String concurrency);

    DomainMessageListenerContainer createSendLargeMessageListenerContainer(Domain domain);

    DomainMessageListenerContainer createSplitAndJoinListenerContainer(Domain domain);

    DomainMessageListenerContainer createPullReceiptListenerContainer(Domain domain);

    DomainMessageListenerContainer createRetentionListenerContainer(Domain domain);

    DomainMessageListenerContainer createPullMessageListenerContainer(Domain domain);
}
