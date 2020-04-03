package eu.domibus.rest.validators;

import eu.domibus.web.rest.validators.ItemsWhiteListed;
import org.springframework.stereotype.Component;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Delegate class for ItemsBlacklistValidator
 */
@Component
public class ItemsBlacklistValidatorDelegate extends BaseBlacklistValidatorDelegate<ItemsWhiteListed, String[]> {

}
