package eu.domibus.ebms3.sender;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service(value = "domibusDLQListener")
public class DomibusDLQListener implements MessageListener {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusDLQListener.class);

    @Autowired
    protected DomainContextProvider domainContextProvider;

    public void onMessage(final Message message) {
        domainContextProvider.setCurrentDomain(DomainService.DEFAULT_DOMAIN);

        LOG.info("Receiving message ");
    }
}
