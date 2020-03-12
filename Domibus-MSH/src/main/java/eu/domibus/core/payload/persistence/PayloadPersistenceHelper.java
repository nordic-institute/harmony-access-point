package eu.domibus.core.payload.persistence;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class PayloadPersistenceHelper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PayloadPersistenceHelper.class);

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    boolean isPayloadEncryptionActive(UserMessage userMessage) {
        //EDELIVERY-4749 - SplitAndJoin limitation
        final boolean isPayloadEncryptionActive = !userMessage.isSplitAndJoin() && domibusConfigurationService.isPayloadEncryptionActive(domainContextProvider.getCurrentDomain());
        LOG.debug("Is payload encryption active? [{}]", isPayloadEncryptionActive);
        return isPayloadEncryptionActive;
    }
}
