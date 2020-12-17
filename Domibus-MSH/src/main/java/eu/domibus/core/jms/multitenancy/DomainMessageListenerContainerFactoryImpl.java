package eu.domibus.core.jms.multitenancy;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import static eu.domibus.core.jms.multitenancy.MessageListenerContainerConfiguration.DISPATCH_CONTAINER;
import static eu.domibus.core.jms.multitenancy.MessageListenerContainerConfiguration.PULL_MESSAGE_CONTAINER;
import static eu.domibus.core.jms.multitenancy.MessageListenerContainerConfiguration.PULL_RECEIPT_CONTAINER;
import static eu.domibus.core.jms.multitenancy.MessageListenerContainerConfiguration.RETENTION_CONTAINER;
import static eu.domibus.core.jms.multitenancy.MessageListenerContainerConfiguration.SEND_LARGE_MESSAGE_CONTAINER;
import static eu.domibus.core.jms.multitenancy.MessageListenerContainerConfiguration.SPLIT_AND_JOIN_CONTAINER;
/**
 * @author Ion Perpegel
 * @since 4.0
 *
 * Class for creating Message Listener containers for a specified domain
 */
@Service
public class DomainMessageListenerContainerFactoryImpl implements DomainMessageListenerContainerFactory {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainMessageListenerContainerFactoryImpl.class);

    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public DomainMessageListenerContainer createSendMessageListenerContainer(Domain domain, String selector, String concurrencyPropertyName) {
        LOG.debug("Creating the SendMessageListenerContainer for domain [{}] with selector [{}] and concurrency [{}]", domain, selector, concurrencyPropertyName);
        return (DomainMessageListenerContainer) applicationContext.getBean(DISPATCH_CONTAINER, domain, selector, concurrencyPropertyName);
    }

    @Override
    public DomainMessageListenerContainer createSendLargeMessageListenerContainer(Domain domain) {
        LOG.debug("Creating the SendLargeMessageListenerContainer for domain [{}]", domain);
        return (DomainMessageListenerContainer) applicationContext.getBean(SEND_LARGE_MESSAGE_CONTAINER, domain);
    }

    @Override
    public DomainMessageListenerContainer createSplitAndJoinListenerContainer(Domain domain) {
        LOG.debug("Creating the SplitAndJoinListenerContainer for domain [{}]", domain);
        return (DomainMessageListenerContainer) applicationContext.getBean(SPLIT_AND_JOIN_CONTAINER, domain);
    }

    @Override
    public DomainMessageListenerContainer createPullReceiptListenerContainer(Domain domain) {
        LOG.debug("Creating the PullReceiptListenerContainer for domain [{}]", domain);
        return (DomainMessageListenerContainer) applicationContext.getBean(PULL_RECEIPT_CONTAINER, domain);
    }

    @Override
    public DomainMessageListenerContainer createRetentionListenerContainer(Domain domain) {
        LOG.debug("Creating the RetentionListenerContainer for domain [{}]", domain);
        return (DomainMessageListenerContainer) applicationContext.getBean(RETENTION_CONTAINER, domain);
    }

    @Override
    public DomainMessageListenerContainer createPullMessageListenerContainer(Domain domain) {
        LOG.debug("Creating the PullMessageListenerContainer for domain [{}]", domain);
        return (DomainMessageListenerContainer) applicationContext.getBean(PULL_MESSAGE_CONTAINER, domain);
    }

}
