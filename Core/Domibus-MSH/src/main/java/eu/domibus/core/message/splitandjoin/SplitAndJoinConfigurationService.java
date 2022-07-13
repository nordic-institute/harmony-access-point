package eu.domibus.core.message.splitandjoin;

import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Splitting;
import eu.domibus.core.plugin.handler.MessageSubmitterHelper;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 5.1
 * <p>
 * Spin-off from splitAndJoinHelper to break a cyclic dependency
 */
@Service
public class SplitAndJoinConfigurationService {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SplitAndJoinConfigurationService.class);

    public boolean mayUseSplitAndJoin(LegConfiguration legConfiguration) {
        final Splitting splitting = legConfiguration.getSplitting();
        if (splitting == null) {
            LOG.debug("SplitAndJoin is not applicable for the leg [{}]", legConfiguration);
            return false;
        }
        LOG.debug("SplitAndJoin is applicable for the leg [{}]", legConfiguration);
        return true;
    }

}
