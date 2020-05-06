package eu.domibus.core.message.retention;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyMetadataManager;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractDLQListener implements MessageListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AbstractDLQListener.class);

    public static final int LIMIT = 10000;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    protected AtomicInteger count = new AtomicInteger();

    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void onMessage(final Message message) {
        if (!authUtils.isUnsecureLoginAllowed()) {
            authUtils.setAuthenticationToSecurityContext("dlqListener", "dlqListener", AuthRole.ROLE_ADMIN);
        }
        if (count.get() == 0) {
            LOG.error("-------------------Starting processing [{}] messages with priority [{}]", LIMIT, getPriority());
        }

        try {
            String messageId = message.getStringProperty(MessageConstants.MESSAGE_ID);
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
            LOG.debug("Processing retention message [{}]", messageId);

            final String domainCode = message.getStringProperty(MessageConstants.DOMAIN);
            LOG.debug("Processing message [{}] for domain [{}]", messageId, domainCode);
            domainContextProvider.setCurrentDomain(domainCode);

            Long sleepTime = domibusPropertyProvider.getLongProperty(DomibusPropertyMetadataManager.DOMIBUS_DLQ_SLEEP);
            LOG.debug("Sleeping for [{}]", sleepTime);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                LOG.error("Error sleeping", e);
            }
            int value = count.incrementAndGet();
            if (value == LIMIT) {
                LOG.error("-------------------Finished processing [{}] messages with priority [{}]", LIMIT, getPriority());
                count.set(0);
            }
        } catch (final JMSException e) {
            LOG.error("Error processing JMS message", e);
        }
    }

    protected abstract String getPriority();
}
