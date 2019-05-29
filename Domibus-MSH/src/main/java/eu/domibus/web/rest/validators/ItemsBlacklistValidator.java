package eu.domibus.web.rest.validators;

import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Custom validator that checks that the value does not contain any char from the blacklist
 *
 * @author Ion Perpegel
 * @since 4.1
 */
@Component
public class ItemsBlacklistValidator extends BaseBlacklistValidator<ItemsNotBlacklisted, List<String>> {

    @Override
    protected String getErrorMessage() {
        return ItemsNotBlacklisted.MESSAGE;
    }

    public boolean isValid(List<String> value) {
        return super.isValidValue(value);
    }

}
