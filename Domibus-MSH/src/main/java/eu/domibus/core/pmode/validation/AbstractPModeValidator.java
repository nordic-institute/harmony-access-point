package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.common.model.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * Abstract implementor of the interface so that the derived classes can implement just one method: with the pMode deserialized or not
 */
public abstract class AbstractPModeValidator implements PModeValidator {

//    @Override
//    public List<PModeIssue> validateAsXml(byte[] xml) {
//        return new ArrayList<>();
//    }

    @Override
    public List<PModeIssue> validate(Configuration configuration) {
        return new ArrayList<>();
    }
}
