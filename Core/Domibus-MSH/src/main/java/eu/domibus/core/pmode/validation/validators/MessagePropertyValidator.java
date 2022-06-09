package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageProperty;
import eu.domibus.api.model.Property;
import eu.domibus.api.model.UserMessage;
import eu.domibus.common.ErrorCode;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.plugin.Submission;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

/**
 * @author Catalin Enache
 * @since 4.1.5
 */
@Service
public class MessagePropertyValidator {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagePropertyValidator.class);

    @Timer(clazz = MessagePropertyValidator.class, value = "validate")
    @Counter(clazz = MessagePropertyValidator.class, value = "validate")
    public void validate(final UserMessage userMessage, MSHRole mshRole) throws EbMS3Exception {
        final String messageId = userMessage.getMessageId();
        LOG.debug("Checking properties size for message [{}]", messageId);

        if (userMessage.getMessageProperties() == null) {
            LOG.debug("no message properties found for message [{}]", messageId);
            return;
        }
        final Set<MessageProperty> properties = userMessage.getMessageProperties();

        for (MessageProperty property : properties) {
            validateProperty(property.getName(), property.getValue(), property.getType(), mshRole, messageId);
        }
    }


    @Timer(clazz = MessagePropertyValidator.class, value = "submissionValidate")
    @Counter(clazz = MessagePropertyValidator.class, value = "submissionValidate")
    public void validate(final Submission submission, MSHRole mshRole) throws EbMS3Exception {
        final String messageId = submission.getMessageId();
        LOG.debug("Checking properties size for message [{}]", messageId);

        if (submission.getMessageProperties() == null) {
            LOG.debug("no message properties found for message [{}]", messageId);
            return;
        }
        final Collection<Submission.TypedProperty> properties = submission.getMessageProperties();

        for (Submission.TypedProperty property : properties) {
            validateProperty(property.getKey(), property.getValue(), property.getType(), mshRole, messageId);
        }
    }

    protected void validateProperty(String name, String value, String type, MSHRole mshRole, String messageId) throws EbMS3Exception {
        if (value != null && value.length() > Property.VALUE_MAX_SIZE) {
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_PROPERTY_SIZE_EXCEEDED, name, Property.VALUE_MAX_SIZE);
            throw  EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                    .message(name + " property has a value which exceeds " + Property.VALUE_MAX_SIZE + " characters size.")
                    .refToMessageId(messageId)
                    .mshRole(mshRole)
                    .build();
        }
    }
}
