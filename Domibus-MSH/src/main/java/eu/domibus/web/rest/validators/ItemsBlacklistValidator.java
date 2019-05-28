package eu.domibus.web.rest.validators;

import com.google.common.base.Strings;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Custom validator that checks that the value does not contain any char from the blacklist
 *
 * @author Ion Perpegel
 * @since 4.1
 */
@Component
public class ItemsBlacklistValidator extends BaseBlacklistValidator<ItemsNotBlacklisted, List<String>> {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(ItemsBlacklistValidator.class);

    @Override
    protected String getErrorMessage() {
        return ItemsNotBlacklisted.MESSAGE;
    }

    public boolean isValid(List<String> value) {
        if (ArrayUtils.isEmpty(blacklist)) {
            return true;
        }
        if (CollectionUtils.isEmpty(value)) {
            return true;
        }

        return value.stream().allMatch(el -> this.isValid(el));
    }

    private boolean isValid(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return true;
        }

        return !Arrays.stream(blacklist).anyMatch(el -> value.contains(el.toString()));
    }
}
