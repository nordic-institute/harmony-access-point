package eu.domibus.web.rest.validators;

import eu.domibus.api.validators.CustomWhiteListed;
import eu.domibus.rest.validators.FieldBlacklistValidatorDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Ion Perpegel
 * @since 4.1
 * <p>
 * Custom validator that checks that the string value does not contain any char from the blacklist but only chars from the whitelist
 * It heavily relies on the base class
 */
@Component
public class FieldBlacklistValidator extends BaseBlacklistValidator<FieldWhiteListed, String> {

    @Autowired
    FieldBlacklistValidatorDelegate fieldBlacklistValidatorDelegate;

    @PostConstruct
    public void onInit() {
        fieldBlacklistValidatorDelegate.setBaseBlacklistValidator(this);
    }

    @Override
    public String getErrorMessage() {
        return FieldWhiteListed.MESSAGE;
    }

    @Override
    public boolean isValid(String value, CustomWhiteListed customAnnotation) {
        return super.isValidValue(value, customAnnotation);
    }

}
