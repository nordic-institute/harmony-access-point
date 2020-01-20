package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.common.model.configuration.Configuration;

import java.util.List;

public interface PModeValidationService {
    List<PModeIssue> validate(byte[] rawConfiguration, Configuration configuration);
}
