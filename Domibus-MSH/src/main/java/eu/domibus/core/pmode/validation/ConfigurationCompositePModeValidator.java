package eu.domibus.core.pmode.validation;

import eu.domibus.api.property.DomibusPropertyProvider;
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
public class ConfigurationCompositePModeValidator extends CompositePModeValidator {

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @PostConstruct
    public void Init() {
        Set<String> propNames = domibusPropertyProvider.getPropertyNames(s -> s.startsWith(DOMIBUS_P_MODE_VALIDATION_X_PATH_VALIDATOR));
        propNames.forEach(propName -> {
            String propVal = domibusPropertyProvider.getProperty(propName);

            String[] properties = propVal.split(";");
            String targetExpression = properties[0];
            String acceptedValuesExpression = properties[1];
            String errorMessage = null;
            if (properties.length == 3) {
                errorMessage = properties[2];
            }
            this.getValidators().add(new XPathPModeValidator(targetExpression, acceptedValuesExpression, errorMessage));
        });
    }
}
