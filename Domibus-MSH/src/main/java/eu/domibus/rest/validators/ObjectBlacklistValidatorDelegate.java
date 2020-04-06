package eu.domibus.rest.validators;

import eu.domibus.web.rest.validators.ObjectWhiteListed;
import org.springframework.stereotype.Component;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Delegate class for ObjectBlacklistValidator
 */
@Component
public class ObjectBlacklistValidatorDelegate extends BaseBlacklistValidatorDelegate<ObjectWhiteListed, Object> {
}
