package eu.domibus.web.rest.validators;

import org.springframework.stereotype.Component;

/**
 * @author Ion Perpegel
 * @since 4.1
 *
 * Custom validator that checks that the string value does not contain any char from the blacklist but only chars from the whitelist
 * It heavily relies on the base class
 */
@Component
public class BlacklistValidator extends BaseBlacklistValidator<WhiteListed, String> {

    @Override
    protected String getErrorMessage() {
        return WhiteListed.MESSAGE;
    }

    public boolean isValid(String value) {
        return super.isValidValue(value);
    }

}
