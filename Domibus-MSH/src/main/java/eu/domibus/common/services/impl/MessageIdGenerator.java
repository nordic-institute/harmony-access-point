
package eu.domibus.common.services.impl;

import eu.domibus.api.property.DomibusPropertyProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS_MSH_MESSAGEID_SUFFIX;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class MessageIdGenerator {
    private static final String MESSAGE_ID_SUFFIX_PROPERTY = DOMIBUS_MSH_MESSAGEID_SUFFIX;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Transactional(propagation = Propagation.SUPPORTS)
    public String generateMessageId() {
        String messageIdSuffix = domibusPropertyProvider.getDomainProperty(MESSAGE_ID_SUFFIX_PROPERTY);
        return UUID.randomUUID() + "@" + messageIdSuffix;
    }
}
