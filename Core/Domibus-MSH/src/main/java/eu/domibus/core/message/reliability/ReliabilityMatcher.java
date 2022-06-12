package eu.domibus.core.message.reliability;

import eu.domibus.common.model.configuration.Reliability;
import eu.domibus.core.message.reliability.ReliabilityChecker;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public interface ReliabilityMatcher {

    boolean matchReliableCallBack(final Reliability reliability);

    boolean matchReliableReceipt(final Reliability reliability);

    ReliabilityChecker.CheckResult fails();


}
