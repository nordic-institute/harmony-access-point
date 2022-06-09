package eu.domibus.core.jms.multitenancy;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import static eu.domibus.core.jms.multitenancy.MessageListenerContainerConfiguration.*;

/**
 * @author Ion Perpegel
 * @since 4.0
 *
 * Class for creating Message Listener containers for a specified domain
 */
@Service
public class DomainMessageListenerContainerFactoryImpl implements DomainMessageListenerContainerFactory {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainMessageListenerContainerFactoryImpl.class);

    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public DomainMessageListenerContainerImpl createSendMessageListenerContainer(Domain domain, String selector, String concurrencyPropertyName) {
        LOG.debug("Creating the SendMessageListenerContainer for domain [{}] with selector [{}] and concurrency [{}]", domain, selector, concurrencyPropertyName);
        return (DomainMessageListenerContainerImpl) applicationContext.getBean(DISPATCH_CONTAINER, domain, selector, concurrencyPropertyName);
    }

    @Override
    public DomainMessageListenerContainerImpl createSendLargeMessageListenerContainer(Domain domain) {
        LOG.debug("Creating the SendLargeMessageListenerContainer for domain [{}]", domain);
        return (DomainMessageListenerContainerImpl) applicationContext.getBean(SEND_LARGE_MESSAGE_CONTAINER, domain);
    }

    @Override
    public DomainMessageListenerContainerImpl createSplitAndJoinListenerContainer(Domain domain) {
        LOG.debug("Creating the SplitAndJoinListenerContainer for domain [{}]", domain);
        return (DomainMessageListenerContainerImpl) applicationContext.getBean(SPLIT_AND_JOIN_CONTAINER, domain);
    }

    @Override
    public DomainMessageListenerContainerImpl createPullReceiptListenerContainer(Domain domain) {
        LOG.debug("Creating the PullReceiptListenerContainer for domain [{}]", domain);
        return (DomainMessageListenerContainerImpl) applicationContext.getBean(PULL_RECEIPT_CONTAINER, domain);
    }

    @Override
    public DomainMessageListenerContainerImpl createRetentionListenerContainer(Domain domain) {
        LOG.debug("Creating the RetentionListenerContainer for domain [{}]", domain);
        return (DomainMessageListenerContainerImpl) applicationContext.getBean(RETENTION_CONTAINER, domain);
    }

    @Override
    public DomainMessageListenerContainerImpl createPullMessageListenerContainer(Domain domain) {
        LOG.debug("Creating the PullMessageListenerContainer for domain [{}]", domain);
        return (DomainMessageListenerContainerImpl) applicationContext.getBean(PULL_MESSAGE_CONTAINER, domain);
    }

    @Override
    public DomainMessageListenerContainerImpl createEArchiveMessageListenerContainer(Domain domain) {
        LOG.debug("Creating the EArchiveMessageListenerContainer for domain [{}]", domain);
        return (DomainMessageListenerContainerImpl) applicationContext.getBean(EARCHIVE_CONTAINER, domain);
    }
    @Override
    public DomainMessageListenerContainerImpl createEArchiveNotificationListenerContainer(Domain domain) {
        LOG.debug("Creating the EArchiveNotificationListenerContainer for domain [{}]", domain);
        return (DomainMessageListenerContainerImpl) applicationContext.getBean(EARCHIVE_NOTIF_CONTAINER, domain);
    }
    @Override
    public DomainMessageListenerContainerImpl createEArchiveNotificationDlqListenerContainer(Domain domain) {
        LOG.debug("Creating the EArchiveNotificationDlqListenerContainer for domain [{}]", domain);
        return (DomainMessageListenerContainerImpl) applicationContext.getBean(EARCHIVE_NOTIF_DLQ_CONTAINER, domain);
    }

}
