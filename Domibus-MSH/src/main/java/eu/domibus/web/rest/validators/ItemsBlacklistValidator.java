package eu.domibus.web.rest.validators;

import eu.domibus.api.validators.CustomWhiteListed;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.rest.validators.BlacklistValidatorDelegate;
import eu.domibus.rest.validators.ItemsBlacklistValidatorDelegate;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * Custom validator that checks that all Strings in the array do not contain any char from the blacklist
 *
 * @author Ion Perpegel
 * @since 4.1
 */
@Component
public class ItemsBlacklistValidator extends BaseBlacklistValidator<ItemsWhiteListed, String[]> {
    private static final Logger LOG = DomibusLoggerFactory.getLogger(ItemsBlacklistValidator.class);

    @Autowired
    ItemsBlacklistValidatorDelegate blacklistValidatorDelegate;

    @PostConstruct
    public void init() {
        blacklistValidatorDelegate.setBaseBlacklistValidator(this);
    }

    @Override
    public String getErrorMessage() {
        return ItemsWhiteListed.MESSAGE;
    }

    @Override
    public boolean isValid(String[] values, CustomWhiteListed customAnnotation) {
        List<String> list = Arrays.asList(values);
        if (CollectionUtils.isEmpty(list)) {
            LOG.debug("Collection is empty, exiting");
            return true;
        }
        boolean res = list.stream().allMatch(el -> isValidValue(el, customAnnotation));
        LOG.debug("Validated values: [{}] and the outcome is [{}]", values, res);
        return res;
    }

}
