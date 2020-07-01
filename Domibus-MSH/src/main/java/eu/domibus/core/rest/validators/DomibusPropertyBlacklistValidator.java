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
    private String message = DomibusPropertyWhiteListed.MESSAGE;

    @Override
    public String getErrorMessage() {
        return message;
    }

    @Override
    public boolean isValid(DomibusProperty property, CustomWhiteListed customAnnotation) {
//        customAnnotation = getCustomWhiteListedChars(property);

        if (property.getMetadata().getTypeAsEnum() != DomibusPropertyMetadata.Type.STRING) {
            // no need for validation as it will be performed by the type validation
            LOG.debug("Skip black-list validation for property [{}] of type [{}]",
                    property.getMetadata().getName(), property.getMetadata().getType());
            return true;
        }
        // apply ordinary blacklist validation
        LOG.debug("Perform black-list validation for property [{}] as it is of STRING type.",
                property.getMetadata().getName());
        return super.isValidValue(property.getValue());
    }

//    private CustomWhiteListed getCustomWhiteListedChars(DomibusProperty property) {
//        return new CustomWhiteListedImpl(property.getMetadata().getTypeEnum().getRegularExpression());
//    }

//    class CustomWhiteListedImpl implements CustomWhiteListed {
//
//        String permittedChars;
//
//        public CustomWhiteListedImpl(String permittedChars) {
//            this.permittedChars = permittedChars;
//        }
//
//        @Override
//        public String permitted() {
//            return permittedChars;
//        }
//
//        @Override
//        public Class<? extends Annotation> annotationType() {
//            return CustomWhiteListed.class;
//        }
//    }
}
