package eu.domibus.api.property.validators;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;

public class RegexpValidator implements DomibusPropertyValidator {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RegexpValidator.class);

    String regexp;

    public RegexpValidator(String regexp) {
        this.regexp = regexp;
    }

    @Override
    public boolean isValid(String propValue) {
        if (StringUtils.isBlank(this.regexp)) {
            LOG.debug("Regular expression for property type is null; exiting validation.");
            return true;
        }
        if (StringUtils.isBlank(propValue)) {
            LOG.debug("Property value is null; exiting validation.");
            return true;
        }
        return propValue.matches(this.regexp);
    }
}
