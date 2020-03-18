package eu.domibus.core.message.pull;

import eu.domibus.common.model.configuration.Reliability;
import eu.domibus.common.model.configuration.ReplyPattern;
import eu.domibus.core.message.reliability.ReliabilityChecker;
import eu.domibus.core.message.reliability.ReliabilityMatcher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

@Component
@Qualifier("pullRequestMatcher")
public class PullRequestMatcher implements ReliabilityMatcher {
    @Override
    public boolean matchReliableCallBack(Reliability reliability) {
        if (reliability == null) {
            return false;
        }

        return (ReplyPattern.RESPONSE.equals(reliability.getReplyPattern())
                || ReplyPattern.CALLBACK.equals(reliability.getReplyPattern()));
    }

    @Override
    public boolean matchReliableReceipt(Reliability reliability) {
        return false;
    }

    @Override
    public ReliabilityChecker.CheckResult fails() {
        return ReliabilityChecker.CheckResult.PULL_FAILED;
    }
}
