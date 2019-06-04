package eu.domibus.web.rest.validators;

import org.springframework.stereotype.Component;

/**
 * Custom validator that checks that the value does not contain any char from the blacklist but only chars from the whitelist
 *
 * @author Ion Perpegel
 * @since 4.1
 */
@Component
public class BlacklistValidator extends BaseBlacklistValidator<NotBlacklisted, String> {

    @Override
    protected String getErrorMessage() {
        return NotBlacklisted.MESSAGE;
    }

    public boolean isValid(String value) {
        return super.isValidValue(value);
    }

}
