package eu.domibus.web.rest.validators;

import org.springframework.stereotype.Component;

/**
 * Custom validator that checks that all Strings in the array do not contain any char from the blacklist
 *
 * @author Ion Perpegel
 * @since 4.1
 */
@Component
public class ItemsBlacklistValidator extends BaseBlacklistValidator<ItemsNotBlacklisted, String[]> {

    @Override
    protected String getErrorMessage() {
        return ItemsNotBlacklisted.MESSAGE;
    }

    @Override
    public boolean isValid(String[] value) {
        return super.isValidValue(value);
    }

}
