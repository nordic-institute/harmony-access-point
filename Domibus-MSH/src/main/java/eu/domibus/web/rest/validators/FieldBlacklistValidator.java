package eu.domibus.web.rest.validators;

import eu.domibus.api.validators.CustomWhiteListed;
import org.springframework.stereotype.Component;

/**
 * @author Ion Perpegel
 * @since 4.1
 * <p>
 * Custom validator that checks that the string value does not contain any char from the blacklist but only chars from the whitelist
 * It heavily relies on the base class
 */
@Component
public class FieldBlacklistValidator extends BaseBlacklistValidator<FieldWhiteListed, String> {

    @Override
    public String getErrorMessage() {
        return FieldWhiteListed.MESSAGE;
    }

    @Override
    public boolean isValid(String value, CustomWhiteListed customAnnotation) {
        return super.isValidValue(value, customAnnotation);
    }

}
