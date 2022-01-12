package eu.domibus.ext.delegate.services.message;

import eu.domibus.api.message.validation.UserMessageValidatorServiceDelegate;
import eu.domibus.core.spi.validation.UserMessageValidatorSpi;
import eu.domibus.ext.delegate.mapper.DomibusExtMapper;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserMessageValidatorServiceDelegateImpl implements UserMessageValidatorServiceDelegate {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageValidatorServiceDelegateImpl.class);

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

    protected boolean isUserMessageValidatorActive() {
        return userMessageValidatorSpi != null;
    }
}
