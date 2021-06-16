package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.model.MessageProperty;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Property;
import eu.domibus.common.model.configuration.PropertySet;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.message.UserMessageServiceHelper;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Koch, Stefan Mueller
 * @version 3.0
 * @since 3.0
 */

@Service
public class PropertyProfileValidator {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PropertyProfileValidator.class);

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    UserMessageServiceHelper userMessageDefaultServiceHelper;

    @Autowired
    DomibusConfigurationService domibusConfigurationService;

    public void validate(final UserMessage userMessage, final String pmodeKey) throws EbMS3Exception {
        final List<Property> modifiablePropertyList = new ArrayList<>();
        final LegConfiguration legConfiguration = this.pModeProvider.getLegConfiguration(pmodeKey);
        final PropertySet propSet = legConfiguration.getPropertySet();
        if (propSet == null || CollectionUtils.isEmpty(propSet.getProperties())) {
            LOG.businessInfo(DomibusMessageCode.BUS_PROPERTY_PROFILE_VALIDATION_SKIP, legConfiguration.getName());
            // no profile means everything is valid
            return;
        }

        final Set<Property> profile = propSet.getProperties();

        Set<MessageProperty> messageProperties = userMessage.getMessageProperties();
        modifiablePropertyList.addAll(profile);
        if (userMessage.getMessageProperties() != null) {
            checkDuplicateMessageProperties(modifiablePropertyList, messageProperties);
        }

        for (final eu.domibus.api.model.Property property : messageProperties) {
            Property profiled = null;
            for (final Property profiledProperty : modifiablePropertyList) {
                if (profiledProperty.getKey().equalsIgnoreCase(property.getName())) {
                    profiled = profiledProperty;
                    break;
                }
            }
            modifiablePropertyList.remove(profiled);
            if (profiled == null) {
                LOG.businessError(DomibusMessageCode.BUS_PROPERTY_MISSING, property.getName());
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Property profiling for this exchange does not include a property named [" + property.getName() + "]", userMessage.getMessageId(), null);
            }

            switch (profiled.getDatatype().toLowerCase()) {
                case "string":
                    break;
                case "int":
                    try {
                        Integer.parseInt(property.getValue()); //NOSONAR: Validation is done via exception
                        break;
                    } catch (final NumberFormatException e) {
                        throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Property profiling for this exchange requires a INTEGER datatype for property named: " + property.getName() + ", but got " + property.getValue(), userMessage.getMessageId(), null);
                    }
                case "boolean":
                    if (property.getValue().equalsIgnoreCase("false") || property.getValue().equalsIgnoreCase("true")) {
                        break;
                    }
                    throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Property profiling for this exchange requires a BOOLEAN datatype for property named: " + property.getName() + ", but got " + property.getValue(), userMessage.getMessageId(), null);
                default:
                    PropertyProfileValidator.LOG.warn("Validation for Datatype " + profiled.getDatatype() + " not possible. This type is not known by the validator. The value will be accepted unchecked");
            }


        }
        for (final Property property : modifiablePropertyList) {
            if (property.isRequired()) {
                LOG.businessError(DomibusMessageCode.BUS_PROPERTY_MISSING, property.getName());
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Required property missing [" + property.getName() + "]", userMessage.getMessageId(), null);
            }
        }

        LOG.businessInfo(DomibusMessageCode.BUS_PROPERTY_PROFILE_VALIDATION, propSet.getName());
    }

    protected void checkDuplicateMessageProperties(List<Property> modifiablePropertyList, Set<MessageProperty> messageProperties) throws EbMS3Exception {
        for (final Property profiledProperty : modifiablePropertyList) {
            int duplicateMessagePropertiesCount = (int) messageProperties.stream().filter(string -> string.getName().equalsIgnoreCase(profiledProperty.getKey())).count();
            if (duplicateMessagePropertiesCount > 1) {
                LOG.businessError(DomibusMessageCode.BUS_PROPERTY_DUPLICATE, profiledProperty.getKey());
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0052, "Duplicate Message property found for property name [" + profiledProperty.getKey() + "]", null, null);
            }
        }
    }
}
