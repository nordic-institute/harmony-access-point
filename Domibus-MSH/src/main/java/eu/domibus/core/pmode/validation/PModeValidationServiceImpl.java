package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.IssueLevel;
import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.model.configuration.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS_P_MODE_VALIDATION_WARNINGS_AS_ERRORS;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Implementation class for pMode validation: Calls al validators(old style and new) and aggregates the results
 */
@Service
public class PModeValidationServiceImpl implements PModeValidationService {

    @Autowired(required = false)
    protected List<PModeValidator> pModeValidatorList;

//    @Autowired(required = false)
//    protected List<ConfigurationValidator> configurationValidators;

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    public List<PModeIssue> validate(byte[] rawConfiguration, Configuration configuration) {
        boolean warningsAsErrors = domibusPropertyProvider.getBooleanProperty(DOMIBUS_P_MODE_VALIDATION_WARNINGS_AS_ERRORS);

        List<PModeIssue> issues = new ArrayList<>();

        configuration.preparePersist(); // TODO: review this

//        for (ConfigurationValidator validator : configurationValidators) {
//            List<String> messages = validator.validate(configuration);
//            issues.addAll(messages.stream().map(m -> new PModeIssue(m, IssueLevel.WARNING)).collect(Collectors.toList()));
//        }
        for (PModeValidator validator : pModeValidatorList) {
            issues.addAll(validator.validateAsConfiguration(configuration));
        }
        for (PModeValidator validator : pModeValidatorList) {
            issues.addAll(validator.validateAsXml(rawConfiguration));
        }

        if (warningsAsErrors) {
            issues.forEach(issue -> issue.setLevel(IssueLevel.ERROR));
        }

        return issues;
    }
}