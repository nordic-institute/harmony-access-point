package eu.domibus.core.message.pull;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.Validate;

import java.util.Collection;

/**
 * @author Thomas Dussart
 * @since 3.3
 * <p>
 * Contextual class for a pull.
 */
public class PullContext {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PullContext.class);

    private Process process;
    private Party responder;
    private Party initiator;
    private String mpcQualifiedName;
    public static final String MPC = "mpc";
    public static final String PULL_REQUEST_ID = "pullRequestId";
    public static final String PMODE_KEY = "pmodKey";
    public static final String NOTIFY_BUSINNES_ON_ERROR = "NOTIFY_BUSINNES_ON_ERROR";

    public PullContext(final Process process, final Party responder, final Party initiator, final String mpcQualifiedName) {
        Validate.notNull(process);
        Validate.notNull(responder);
        Validate.notNull(mpcQualifiedName);
        this.process = process;
        this.mpcQualifiedName = mpcQualifiedName;
        this.responder = responder;
        this.initiator = initiator;
    }

    public String getAgreement() {
        if (process.getAgreement() != null) {
            return process.getAgreement().getName();
        }
        return "";
    }

    public Process getProcess() {
        return process;
    }

    public Party getInitiator() {
        if (process.isDynamicInitiator()) {
            // FIXME: not sure if the check for no initiator parties is still needed -> Ion Perpegel 03-01-25 [EDELIVERY-12876] PULL refactoring
            if (CollectionUtils.isEmpty(process.getInitiatorParties())) {
                return null;
            }
            return initiator;
        } else {
            // no initiator parties - return null
            if (CollectionUtils.isEmpty(process.getInitiatorParties())) {
                LOG.warn("No initiator parties found for static-initiator pull process [{}]", process.getName());
                return null;
            }
            // exactly one initiator party - all is good
            if (CollectionUtils.size(process.getInitiatorParties()) == 1) {
                return process.getInitiatorParties().iterator().next();
            }
            // more than one initiator party - warning but continue with the first one
            initiator = process.getInitiatorParties().iterator().next();
            LOG.warn("[{}] initiator parties found for pull process [{}], the first one will be used: [{}]", CollectionUtils.size(process.getInitiatorParties()), process.getName(), initiator.getName());
            return initiator;
        }
    }

    public String getMpcQualifiedName() {
        return mpcQualifiedName;
    }

    public Party getResponder() {
        return responder;
    }

    public LegConfiguration filterLegOnMpc() {
        if (mpcQualifiedName != null) {
            Collection<LegConfiguration> filter = Collections2.filter(process.getLegs(), new Predicate<LegConfiguration>() {
                @Override
                public boolean apply(LegConfiguration legConfiguration) {
                    return mpcQualifiedName.equalsIgnoreCase(legConfiguration.getDefaultMpc().getQualifiedName());
                }
            });

            LegConfiguration legConfiguration = filter.iterator().next();
            if (CollectionUtils.size(filter) > 1) {
                LOG.debug("[{}] legs found for mpc [{}], the first one will be used: [{}]", CollectionUtils.size(filter), mpcQualifiedName, legConfiguration.getName());
            }

            return legConfiguration;
        } else throw new IllegalArgumentException("Method should be called after correct context setup.");
    }


}
