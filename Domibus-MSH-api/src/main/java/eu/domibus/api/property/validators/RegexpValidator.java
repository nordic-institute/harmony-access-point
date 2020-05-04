package eu.domibus.api.property.validators;

import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;

public class RegexpValidator implements DomibusPropertyValidator {
    private static final Logger LOG = DomibusLoggerFactory.getLogger(RegexpValidator.class);

    String regexp;

    public RegexpValidator(String regexp) {
        this.regexp = regexp;
    }

    @Override
    public boolean isValid(String propValue) {
        if (this.regexp == null) {
            LOG.debug("Regular expression for property type is null; exiting validation.");
            return true;
        }
        if (propValue == null) {
            LOG.debug("Property value is nukk; exiting validation.");
            return true;
        }
        return propValue.matches(this.regexp);
    }
}
