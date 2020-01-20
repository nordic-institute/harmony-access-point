package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.common.model.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPModeValidator implements PModeValidator {

    public List<PModeIssue> validateAsXml(byte[] xml) {
        return new ArrayList<>();
    }

    public List<PModeIssue> validateAsConfiguration(Configuration configuration) {
        return new ArrayList<>();
    }
}
