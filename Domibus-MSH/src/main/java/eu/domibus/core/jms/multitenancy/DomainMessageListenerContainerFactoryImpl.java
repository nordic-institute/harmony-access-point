package eu.domibus.core.jms.multitenancy;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.0
 * <p>
 * Class for creating Message Listener containers for a specified domain
 */
@Service
public class DomainMessageListenerContainerFactoryImpl implements DomainMessageListenerContainerFactory {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainMessageListenerContainerFactoryImpl.class);

    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public DomainMessageListenerContainer createSendMessageListenerContainer(Domain domain) {
        LOG.debug("Creating the SendMessageListenerContainer for domain [{}]", domain);
        return (DomainMessageListenerContainer) applicationContext.getBean("dispatchContainer", domain);
    }

    @Override
    public DomainMessageListenerContainer createDlqListenerContainerLowPriority(Domain domain, String selector, String concurrency) {
        LOG.debug("Creating the SendMessageListenerContainer for domain [{}] and selector [{}] and concurrency [{}]", domain, selector, concurrency);
        return (DomainMessageListenerContainer) applicationContext.getBean("dlqListenerLowPriority", domain, selector, concurrency);
    }

    @Override
    public DomainMessageListenerContainer createDlqListenerContainerMediumPriority(Domain domain, String selector, String concurrency) {
        LOG.debug("Creating the SendMessageListenerContainer for domain [{}] and selector [{}] and concurrency [{}]", domain, selector, concurrency);
        return (DomainMessageListenerContainer) applicationContext.getBean("dlqListenerMediumPriority", domain, selector, concurrency);
    }

    @Override
    public DomainMessageListenerContainer createDlqListenerContainerHighPriority(Domain domain, String selector, String concurrency) {
        LOG.debug("Creating the SendMessageListenerContainer for domain [{}] and selector [{}] and concurrency [{}]", domain, selector, concurrency);
        return (DomainMessageListenerContainer) applicationContext.getBean("dlqListenerHighPriority", domain, selector, concurrency);
    }

    @Override
    public DomainMessageListenerContainer createSendLargeMessageListenerContainer(Domain domain) {
        LOG.debug("Creating the SendLargeMessageListenerContainer for domain [{}]", domain);
        return (DomainMessageListenerContainer) applicationContext.getBean("sendLargeMessageContainer", domain);
    }

    @Override
    public DomainMessageListenerContainer createSplitAndJoinListenerContainer(Domain domain) {
        LOG.debug("Creating the SplitAndJoinListenerContainer for domain [{}]", domain);
        return (DomainMessageListenerContainer) applicationContext.getBean("splitAndJoinContainer", domain);
    }

    @Override
    public DomainMessageListenerContainer createPullReceiptListenerContainer(Domain domain) {
        LOG.debug("Creating the PullReceiptListenerContainer for domain [{}]", domain);
        return (DomainMessageListenerContainer) applicationContext.getBean("pullReceiptContainer", domain);
    }

    @Override
    public DomainMessageListenerContainer createRetentionListenerContainer(Domain domain) {
        LOG.debug("Creating the RetentionListenerContainer for domain [{}]", domain);
        return (DomainMessageListenerContainer) applicationContext.getBean("retentionContainer", domain);
    }

    @Override
    public DomainMessageListenerContainer createPullMessageListenerContainer(Domain domain) {
        LOG.debug("Creating the PullMessageListenerContainer for domain [{}]", domain);
        return (DomainMessageListenerContainer) applicationContext.getBean("pullMessageContainer", domain);
    }

}
