package eu.domibus.core.message.splitandjoin;

import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Splitting;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 5.0
 * <p>
 * Spin-off from splitAndJoinHelper to break a cyclic dependency
 */
@Service
public class SplitAndJoinConfigurationService {

    public boolean mayUseSplitAndJoin(LegConfiguration legConfiguration) {
        final Splitting splitting = legConfiguration.getSplitting();
        if (splitting == null) {
            return false;
        }
        return true;
    }

}
