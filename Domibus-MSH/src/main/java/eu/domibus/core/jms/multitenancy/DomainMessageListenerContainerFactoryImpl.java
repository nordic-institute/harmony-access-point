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
    public DomainMessageListenerContainer createSendMessageListenerContainer(Domain domain, String selector, String concurrencyPropertyName) {
        LOG.debug("Creating the SendMessageListenerContainer for domain [{}] with selector [{}] and concurrency [{}]", domain, selector, concurrencyPropertyName);
        return (DomainMessageListenerContainer) applicationContext.getBean("dispatchContainer", domain, selector, concurrencyPropertyName);
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
