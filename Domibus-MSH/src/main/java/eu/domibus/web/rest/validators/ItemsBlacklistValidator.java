package eu.domibus.web.rest.validators;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Custom validator that checks that the value does not contain any char from the blacklist
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
