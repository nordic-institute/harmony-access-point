package eu.domibus.rest.validators;

import eu.domibus.web.rest.validators.ObjectPropertiesMapBlacklistValidator;
import eu.domibus.web.rest.validators.ObjectWhiteListed;
import org.springframework.stereotype.Component;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Delegate class for ObjectPropertiesMapBlacklistValidator
 */
@Component
public class ObjectPropertiesMapBlacklistValidatorDelegate
        extends BaseBlacklistValidatorDelegate<ObjectWhiteListed, ObjectPropertiesMapBlacklistValidator.Parameter> {
}
