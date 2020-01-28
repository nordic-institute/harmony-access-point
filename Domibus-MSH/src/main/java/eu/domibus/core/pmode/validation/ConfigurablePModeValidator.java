package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.IssueLevel;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Set;

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

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @PostConstruct
    public void Init() {
        Set<String> propNames = domibusPropertyProvider.getPropertyNames(s -> s.startsWith(DOMIBUS_P_MODE_VALIDATION_X_PATH_VALIDATOR));
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
