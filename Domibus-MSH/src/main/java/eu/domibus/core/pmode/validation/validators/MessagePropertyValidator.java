package eu.domibus.core.pmode.validation.validators;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.Property;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @author Catalin Enache
 * @since 4.1.5
 */
@Service
public class MessagePropertyValidator {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagePropertyValidator.class);


    public void validate(final Messaging messaging, MSHRole mshRole) throws EbMS3Exception {
        final String messageId = messaging.getUserMessage().getMessageInfo().getMessageId();
        LOG.debug("Checking properties size for message [{}]", messageId);

        if (messaging.getUserMessage().getMessageProperties() == null) {
            LOG.debug("no message properties found for message [{}]", messageId);
            return;
        }
        final Set<Property> properties = messaging.getUserMessage().getMessageProperties().getProperty();

        for (Property property : properties) {
            if (property.getValue() != null && property.getValue().length() > Property.VALUE_MAX_SIZE) {
                LOG.businessError(DomibusMessageCode.BUS_MESSAGE_PROPERTY_SIZE_EXCEEDED, property.getName(), Property.VALUE_MAX_SIZE);
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, property.getName() + " property has a value which exceeds " + Property.VALUE_MAX_SIZE + " characters size.", messageId, null);
                ex.setMshRole(mshRole);
                throw ex;
            }
        }
    }
}
