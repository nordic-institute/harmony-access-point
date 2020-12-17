package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.PModeValidationException;
import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Implementation class for pMode validation: Calls all pmode validators and aggregates the results
 */
@Service
public class PModeValidationServiceImpl implements PModeValidationService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PModeValidationServiceImpl.class);

    @Autowired(required = false)
    protected List<PModeValidator> pModeValidatorList;

    @Override
    public List<ValidationIssue> validate(Configuration configuration) throws PModeValidationException {
        List<ValidationIssue> allIssues = new ArrayList<>();

        if(configuration != null) {
            configuration.preparePersist();

            for (PModeValidator validator : pModeValidatorList) {
                List<ValidationIssue> issues1 = validator.validate(configuration);
                allIssues.addAll(issues1);
            }

            if (!allIssues.isEmpty() && allIssues.stream().anyMatch(x -> x.getLevel() == ValidationIssue.Level.ERROR)) {
                throw new PModeValidationException(allIssues);
            }
        }

        return allIssues;
    }
}