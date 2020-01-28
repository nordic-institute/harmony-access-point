package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.IssueLevel;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Set;
import java.util.function.Predicate;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS_P_MODE_VALIDATION_X_PATH_VALIDATOR;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Reads xPath validator configurations and creates a composite validator that contains all of them
 */
@Component
public class ConfigurablePModeValidator extends CompositePModeValidator {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ConfigurablePModeValidator.class);
    protected static final Predicate<String> STRING_PREDICATE = s -> s.startsWith(DOMIBUS_P_MODE_VALIDATION_X_PATH_VALIDATOR);

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @PostConstruct
    public void Init() {
        readConfigurationAndCreateValidators();
    }

    protected void readConfigurationAndCreateValidators() {
        Set<String> propNames = domibusPropertyProvider.getPropertyNames(STRING_PREDICATE);
        propNames.forEach(propName -> {
            String propVal = domibusPropertyProvider.getProperty(propName);
            String[] properties = propVal.split(";");
            if (properties.length < 2) {
                LOG.warn("Wrong configuration value [{}] for property [{}]. There should be at least target and lookup paths configured.", propVal, propName);
                return;
            }

            String targetExpression = properties[0];
            String acceptedValuesExpression = properties[1];

            IssueLevel level = null;
            if (properties.length >= 3) {
                level = IssueLevel.valueOf(properties[2]);
            }

            String errorMessage = null;
            if (properties.length == 4) {
                errorMessage = properties[3];
            }

            this.getValidators().add(new XPathPModeValidator(targetExpression, acceptedValuesExpression, level, errorMessage));
        });
    }


}
