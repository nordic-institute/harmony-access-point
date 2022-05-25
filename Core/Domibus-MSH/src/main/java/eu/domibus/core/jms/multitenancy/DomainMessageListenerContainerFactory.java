package eu.domibus.core.jms.multitenancy;

import eu.domibus.api.multitenancy.Domain;

/**
 * @author Ion Perpegel
 * @since 4.0
 *
 * Interface for creating Message Listener containers for a specified domain
 */
public interface DomainMessageListenerContainerFactory {

    DomainMessageListenerContainerImpl createSendMessageListenerContainer(Domain domain, String selector, String concurrency);

    DomainMessageListenerContainerImpl createSendLargeMessageListenerContainer(Domain domain);

    DomainMessageListenerContainerImpl createSplitAndJoinListenerContainer(Domain domain);

    DomainMessageListenerContainerImpl createPullReceiptListenerContainer(Domain domain);

    DomainMessageListenerContainerImpl createRetentionListenerContainer(Domain domain);

    DomainMessageListenerContainerImpl createPullMessageListenerContainer(Domain domain);

    DomainMessageListenerContainerImpl createEArchiveMessageListenerContainer(Domain domain);
    DomainMessageListenerContainerImpl createEArchiveNotificationListenerContainer(Domain domain);
    DomainMessageListenerContainerImpl createEArchiveNotificationDlqListenerContainer(Domain domain);
}
