package eu.domibus.messaging;

import eu.domibus.api.multitenancy.Domain;
import org.springframework.jms.listener.MessageListenerContainer;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
public interface MessageListenerContainerFactory {

    DomainMessageListenerContainer createSendMessageListenerContainer(Domain domain);

    DomainMessageListenerContainer createSendLargeMessageListenerContainer(Domain domain);

    DomainMessageListenerContainer createSplitAndJoinListenerContainer(Domain domain);

    DomainMessageListenerContainer createPullReceiptListenerContainer(Domain domain);

    DomainMessageListenerContainer createRetentionListenerContainer(Domain domain);
}
