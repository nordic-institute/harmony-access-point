package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.common.model.configuration.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * Composition class: validator that has a list of validators: calls all validators and aggregates the results
 */
@Component
public class CompositePModeValidator extends AbstractPModeValidator {
    protected List<PModeValidator> validators = new ArrayList<>();

//    @Override
//    public List<PModeIssue> validateAsXml(byte[] xmlBytes) {
//        List<PModeIssue> issues = new ArrayList<>();
//        if (validators != null) {
//            validators.forEach(validator -> issues.addAll(validator.validateAsXml(xmlBytes)));
//        }
//        return issues;
//    }

    @Override
    public List<PModeIssue> validateAsConfiguration(Configuration configuration) {
        List<PModeIssue> issues = new ArrayList<>();
        if (validators != null) {
            validators.forEach(validator -> issues.addAll(validator.validateAsConfiguration(configuration)));
        }
        return issues;
    }

    public List<PModeValidator> getValidators() {
        return validators;
    }

    public void setValidators(List<PModeValidator> validators) {
        this.validators = validators;
    }

}
