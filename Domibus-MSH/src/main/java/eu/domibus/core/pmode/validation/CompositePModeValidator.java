package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.common.model.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;

public class CompositePModeValidator extends AbstractPModeValidator {
    List<PModeValidator> validators = new ArrayList<>();

    @Override
    public List<PModeIssue> validateAsXml(byte[] xmlBytes) {
        List<PModeIssue> issues = new ArrayList<>();
        if (validators != null) {
            validators.forEach(validator -> issues.addAll(validator.validateAsXml(xmlBytes)));
        }
        return issues;
    }

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
