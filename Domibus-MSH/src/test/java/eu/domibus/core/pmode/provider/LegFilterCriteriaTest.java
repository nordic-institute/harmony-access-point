package eu.domibus.core.pmode.provider;

import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Process;
import mockit.Injectable;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class LegFilterCriteriaTest {

    @Test
    public void appendProcessMismatchErrorsNullCheck() {
        LegFilterCriteria legFilterCriteria = new LegFilterCriteria(null, null, null, null, null, null, null);
        legFilterCriteria.appendProcessMismatchErrors(null, "");
        assertTrue(legFilterCriteria.getProcessMismatchErrors().isEmpty());
    }

    @Test
    public void appendProcessMismatchErrorsOk(@Injectable Process process) {
        LegFilterCriteria legFilterCriteria = new LegFilterCriteria(null, null, null, null, null, null, null);
        legFilterCriteria.appendProcessMismatchErrors(process, "ErrorDetails");
        assertTrue(legFilterCriteria.getProcessMismatchErrors().containsKey(process));
    }

    @Test
    public void appendLegMismatchErrorsNullCheck() {
        LegFilterCriteria legFilterCriteria = new LegFilterCriteria(null, null, null, null, null, null, null);
        legFilterCriteria.appendLegMismatchErrors(null, "");
        assertTrue(legFilterCriteria.getLegMismatchErrors().isEmpty());
    }

    @Test
    public void appendLegMismatchErrorsOk(@Injectable LegConfiguration legConfiguration) {
        LegFilterCriteria legFilterCriteria = new LegFilterCriteria(null, null, null, null, null, null, null);
        legFilterCriteria.appendLegMismatchErrors(legConfiguration, "ErrorDetails");
        assertTrue(legFilterCriteria.getLegMismatchErrors().containsKey(legConfiguration));
    }
}