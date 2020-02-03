package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS_P_MODE_VALIDATION_LEVEL;
import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS_P_MODE_VALIDATION_WARNINGS_AS_ERRORS;

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

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    public List<PModeIssue> validate(byte[] rawConfiguration, Configuration configuration) {
        boolean warningsAsErrors = domibusPropertyProvider.getBooleanProperty(DOMIBUS_P_MODE_VALIDATION_WARNINGS_AS_ERRORS);
        List<PModeIssue> allIssues = new ArrayList<>();

        configuration.preparePersist();

        for (PModeValidator validator : pModeValidatorList) {
            String validatorName = validator.getClass().getSimpleName();
            String levelPropName = DOMIBUS_P_MODE_VALIDATION_LEVEL + "." + validatorName;
            String level = domibusPropertyProvider.getProperty(levelPropName);

            if ("NONE".equals(level)) {
                LOG.trace("Skipping [{}] pMode validator due to configuration level being NONE.", validatorName);
                continue;
            }

            List<PModeIssue> issues1 = validator.validate(configuration);
//            List<PModeIssue> issues2 = validator.validateAsXml(rawConfiguration);

            if (level != null) {
                try {
                    PModeIssue.Level issueLevel = PModeIssue.Level.valueOf(level);

                    LOG.debug("Setting level=[{}] to all issues of [{}] validator.", validatorName);
                    issues1.forEach(issue -> issue.setLevel(issueLevel));
//                    issues2.forEach(issue -> issue.setLevel(issueLevel));
                } catch (IllegalArgumentException ex) {
                    LOG.warn("Wrong pMode issue level value [{}] red from configuraton for [{}] validator.", level, validatorName);
                }
            }

            allIssues.addAll(issues1);
//            allIssues.addAll(issues2);
        }

        if (warningsAsErrors) {
            LOG.debug("Setting level as error for all issues due to warningsAsErrors being true.");
            allIssues.stream()
                    .filter(el -> el.getLevel() == PModeIssue.Level.WARNING)
                    .forEach(issue -> issue.setLevel(PModeIssue.Level.ERROR));
        }

        return allIssues;
    }
}