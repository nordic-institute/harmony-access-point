package eu.domibus.core.pmode.validation;

import org.springframework.stereotype.Component;

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * Validates service name existence by harnessing the xPathValidator
 */

@Component
public class ServiceNameValidator extends XPathPModeValidator {
    private static final String TARGET_EXPR = "//businessProcesses/legConfigurations/legConfiguration/@service";
    private static final String ACCEPTEDVALUE_EXPR = "//businessProcesses/services/service/@name";

    public ServiceNameValidator() {
        super(TARGET_EXPR, ACCEPTEDVALUE_EXPR, "Service [%s] not found in business processes.");
    }
}
