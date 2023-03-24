package eu.domibus.core.rest.validators;

import eu.domibus.api.property.DomibusProperty;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.validators.DomibusPropertyValidator;
import eu.domibus.logging.DomibusLoggerFactory;
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
public class DomibusPropertyValueValidator {
    private static final Logger LOG = DomibusLoggerFactory.getLogger(DomibusPropertyValueValidator.class);

    private DomibusPropertyProvider domibusPropertyProvider;

    private FieldBlacklistValidator fieldBlacklistValidator;

    public DomibusPropertyValueValidator(DomibusPropertyProvider domibusPropertyProvider,
                                         FieldBlacklistValidator fieldBlacklistValidator) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.fieldBlacklistValidator = fieldBlacklistValidator;
        this.fieldBlacklistValidator.init();
    }

    public void validate(DomibusProperty property) {
        boolean validationEnabled = domibusPropertyProvider.getBooleanProperty(DOMIBUS_PROPERTY_VALIDATION_ENABLED);
        if (!validationEnabled) {
            LOG.debug("Domibus property validation is not enabled. Skip validation.");
            return;
        }

        String propName = property.getMetadata().getName();
        String propValue = property.getValue();
        DomibusPropertyValidator validator = getValidator(property.getMetadata());
        if (validator == null) {
            // apply ordinary blacklist validation when there is no type-specific validation
            LOG.trace("Perform black-list validation for property [{}] as it is of [{}] type.", propName, property.getMetadata().getType());
            fieldBlacklistValidator.validate(propValue);
            return;
        }
        // validate using the specific property-type validator
        if (!validator.isValid(propValue)) {
            String message = "Property value [" + propValue + "] of property [" + propName + "] does not match property type [" + property.getMetadata().getType() + "].";
            throw new DomibusPropertyException(message);
        }
    }

    protected DomibusPropertyValidator getValidator(DomibusPropertyMetadata propertyMetadata) {
        try {
            return propertyMetadata.getTypeAsEnum().getValidator();
        } catch (IllegalArgumentException ex) {
            // it is, theoretically, possible for external plugins to define their own property types
            LOG.warn("Property type [{}] of property [{}] is not known, basic validation will be applied.", propertyMetadata.getType(), propertyMetadata.getName());
            return null;
        }
    }

}
