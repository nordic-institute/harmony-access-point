package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.common.model.configuration.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Validates self party existence by harnessing the xPathValidator
 */
@Component
public class SelfPartyValidator implements PModeValidator {
    private static final String TARGET_EXPR = "//configuration/@party";
    private static final String ACCEPTED_VALUE_EXPR = "//businessProcesses/parties/party/@name";

    public SelfPartyValidator() {

    }

    @Override
    public List<PModeIssue> validateAsConfiguration(Configuration configuration) {
        return null;
    }
}
