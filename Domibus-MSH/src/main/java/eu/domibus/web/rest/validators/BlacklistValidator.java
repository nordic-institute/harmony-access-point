package eu.domibus.web.rest.validators;

import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Custom validator that checks that the value does not contain any char from the blacklist
 *
 * @author Ion Perpegel
 * @since 4.1
 */
@Component
public class BlacklistValidator extends BaseBlacklistValidator<NotBlacklisted, String> {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(BlacklistValidator.class);
    private String message = NotBlacklisted.MESSAGE;

    @Override
    protected String getErrorMessage() {
        return message;
    }

    public boolean isValid(String value) {
        return super.isStringValid(value);
    }

}
