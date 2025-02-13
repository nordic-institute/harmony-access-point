package eu.domibus.core.property.listeners;

import eu.domibus.api.property.*;
import eu.domibus.core.property.DomibusPropertyValidatorService;
import eu.domibus.core.util.WarningUtil;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PROPERTIES_PASSWORD_POLICY_ENFORCE;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PROPERTIES_PASSWORD_POLICY_PATTERN;

/**
 * @author Ionut Breaz
 * @since 5.1.5
 * <p>
 * Validates that passwords match the DOMIBUS_PROPERTIES_PASSWORD_POLICY_PATTERN
 */

@Service
public class PasswordChangeListener implements DomibusPropertyChangeListener {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(PasswordChangeListener.class);

    private final DomibusPropertyProvider domibusPropertyProvider;
    private final DomibusPropertyValidatorService domibusPropertyValidatorService;

    public PasswordChangeListener(DomibusPropertyProvider domibusPropertyProvider, DomibusPropertyValidatorService domibusPropertyValidatorService) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domibusPropertyValidatorService = domibusPropertyValidatorService;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return domibusPropertyProvider.getPropertyType(propertyName) == DomibusPropertyMetadata.Type.PASSWORD;
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        final Pattern passwordPolicyPattern = Pattern.compile(domibusPropertyProvider.getProperty(DOMIBUS_PROPERTIES_PASSWORD_POLICY_PATTERN));

        if (!domibusPropertyValidatorService.passwordMatchesPasswordPolicy(propertyValue, passwordPolicyPattern)) {
            String message = "Property value of property [" + propertyName + "] does not match [" + DOMIBUS_PROPERTIES_PASSWORD_POLICY_PATTERN + "].";
            final boolean enforcePropertiesPasswordPolicy = BooleanUtils.isTrue(domibusPropertyProvider.getBooleanProperty(DOMIBUS_PROPERTIES_PASSWORD_POLICY_ENFORCE));
            if (enforcePropertiesPasswordPolicy) {
                throw new DomibusPropertyException(message);
            } else {
                LOG.warn(WarningUtil.warnOutput(message));
            }
        }
    }

}
