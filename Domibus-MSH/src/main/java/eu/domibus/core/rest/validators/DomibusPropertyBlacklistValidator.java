package eu.domibus.core.rest.validators;

import eu.domibus.api.property.DomibusProperty;
import eu.domibus.api.validators.CustomWhiteListed;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.validators.DomibusPropertyWhiteListed;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PROPERTY_VALIDATION_ENABLED;

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
        boolean enabled = domibusPropertyProvider.getBooleanProperty(DOMIBUS_PROPERTY_VALIDATION_ENABLED);
        if (!enabled) {
            LOG.debug("Domibus property validation is not enabled. Returning true.");
            return true;
        }

        String propName = property.getMetadata().getName();
        if (property.getMetadata().getTypeAsEnum().getValidator() != null) {
            // no need for validation as it will be performed by the property type validator
            LOG.trace("Skip black-list validation for property [{}] of type [{}]", propName, property.getMetadata().getType());
            return true;
        }
        // apply ordinary blacklist validation when there is no type validation
        LOG.trace("Perform black-list validation for property [{}] as it is of STRING type.", propName);
        boolean isValid = super.isValidValue(property.getValue());
        if (!isValid) {
            message = DomibusPropertyWhiteListed.MESSAGE + propName + "'s value: " + property.getValue();
            LOG.debug(message);
        }
        return isValid;
    }

}
