package eu.domibus.web.rest.validators;

import eu.domibus.api.validators.CustomWhiteListed;
import eu.domibus.rest.validators.BlacklistValidatorDelegate;
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
public class BlacklistValidator extends BaseBlacklistValidator<WhiteListed, String> {

    @Autowired
    BlacklistValidatorDelegate blacklistValidatorDelegate;

    @PostConstruct
    public void onInit() {
        blacklistValidatorDelegate.setBaseBlacklistValidator(this);
    }

    @Override
    public String getErrorMessage() {
        return WhiteListed.MESSAGE;
    }

    @Override
    public boolean isValid(String value, CustomWhiteListed customAnnotation) {
        return super.isValidValue(value, customAnnotation);
    }

}
