package eu.domibus.rest.validators;

import eu.domibus.web.rest.validators.WhiteListed;
import org.springframework.stereotype.Component;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Delegate class for BlacklistValidator
 */
@Component
public class BlacklistValidatorDelegate extends BaseBlacklistValidatorDelegate<WhiteListed, String> {

}
