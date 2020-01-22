package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.IssueLevel;
import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.ebms3.common.validators.ConfigurationValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * Implementation class for pMode validation: Calls al validators(old style and new) and aggregates the results
 */
@Service
public class PModeValidationServiceImpl implements PModeValidationService {

    @Autowired(required = false)
    protected List<PModeValidator> pModeValidatorList;

    @Autowired(required = false)
    protected List<ConfigurationValidator> configurationValidators;

    public List<PModeIssue> validate(byte[] rawConfiguration, Configuration configuration) {
        List<PModeIssue> issues = new ArrayList<>();

        configuration.preparePersist(); // TODO: review this

        for (ConfigurationValidator validator : configurationValidators) {
            List<String> messages = validator.validate(configuration);
            issues.addAll(messages.stream().map(m -> new PModeIssue(m, IssueLevel.WARNING)).collect(Collectors.toList()));
        }
        for (PModeValidator validator : pModeValidatorList) {
            issues.addAll(validator.validateAsConfiguration(configuration));
        }
        for (PModeValidator validator : pModeValidatorList) {
            issues.addAll(validator.validateAsXml(rawConfiguration));
        }
        return issues;
    }
}