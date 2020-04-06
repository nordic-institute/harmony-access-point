package eu.domibus.rest.validators;

import eu.domibus.web.rest.validators.FieldWhiteListed;
import org.springframework.stereotype.Component;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Delegate class for BlacklistValidator
 */
@Component
public class FieldBlacklistValidatorDelegate extends BaseBlacklistValidatorDelegate<FieldWhiteListed, String> {

}
