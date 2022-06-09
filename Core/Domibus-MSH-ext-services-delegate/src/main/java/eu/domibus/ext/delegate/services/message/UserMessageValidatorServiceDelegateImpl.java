package eu.domibus.ext.delegate.services.message;

import eu.domibus.api.message.validation.UserMessageValidatorServiceDelegate;
import eu.domibus.core.spi.validation.UserMessageValidatorSpi;
import eu.domibus.ext.delegate.mapper.DomibusExtMapper;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class UserMessageValidatorServiceDelegateImpl implements UserMessageValidatorServiceDelegate {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageValidatorServiceDelegateImpl.class);

    protected UserMessageValidatorSpi userMessageValidatorSpi;
    protected DomibusExtMapper domibusExtMapper;

    public UserMessageValidatorServiceDelegateImpl(@Autowired(required = false) UserMessageValidatorSpi userMessageValidatorSpi,
                                                   DomibusExtMapper domibusExtMapper) {
        this.userMessageValidatorSpi = userMessageValidatorSpi;
        this.domibusExtMapper = domibusExtMapper;
    }

    @Override
    public void validate(eu.domibus.api.usermessage.domain.UserMessage userMessage) {
        if (!isUserMessageValidatorActive()) {
            LOG.debug("Validation skipped: validator SPI is not active");
            return;
        }
        LOG.debug("Validating user message");

        final UserMessageDTO userMessageDto = domibusExtMapper.userMessageToUserMessageDTO(userMessage);
        userMessageValidatorSpi.validateUserMessage(userMessageDto);

        LOG.debug("Finished validating user message");
    }

    @Override
    public void validatePayload(InputStream payload, String mimeType) {
        if (!isUserMessageValidatorActive()) {
            LOG.debug("Validation skipped: validator SPI is not active");
            return;
        }
        LOG.debug("Validating payload");

        userMessageValidatorSpi.validatePayload(payload, mimeType);

        LOG.debug("Finished validating payload");
    }

    protected boolean isUserMessageValidatorActive() {
        return userMessageValidatorSpi != null;
    }
}
