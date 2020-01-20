package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.common.model.configuration.Configuration;

import java.util.List;

public interface PModeValidator {

    List<PModeIssue> validateAsXml(byte[] xml);

    List<PModeIssue> validateAsConfiguration(Configuration configuration);
}
