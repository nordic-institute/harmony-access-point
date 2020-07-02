package eu.domibus.core.rest.validators;

import eu.domibus.api.property.DomibusProperty;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.validators.CustomWhiteListed;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.validators.DomibusPropertyWhiteListed;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Custom validator for domibus property values that takes into account the property type to allow some custom chars
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Component
public class DomibusPropertyBlacklistValidator extends BaseBlacklistValidator<DomibusPropertyWhiteListed, DomibusProperty> {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(DomibusPropertyBlacklistValidator.class);

    private String message;

    @Override
    public String getErrorMessage() {
        return message;
    }

    @Override
    public boolean isValid(DomibusProperty property, CustomWhiteListed customAnnotation) {
        String propName = property.getMetadata().getName();
        if (property.getMetadata().getTypeAsEnum().getValidator() != null) {
            // no need for validation as it will be performed by the type validation
            LOG.trace("Skip black-list validation for property [{}] of type [{}]", propName, property.getMetadata().getType());
            return true;
        }
        // apply ordinary blacklist validation
        LOG.trace("Perform black-list validation for property [{}] as it is of STRING type.", propName);
        boolean isValid = super.isValidValue(property.getValue());
        if (!isValid) {
            message = DomibusPropertyWhiteListed.MESSAGE + propName + "'s value: " + property.getValue();
            LOG.debug(message);
        }
        return isValid;
    }

}
