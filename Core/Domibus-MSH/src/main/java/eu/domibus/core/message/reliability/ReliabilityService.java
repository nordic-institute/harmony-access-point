package eu.domibus.core.message.reliability;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.common.model.configuration.LegConfiguration;

/**
 * Service in charge or handling the states of messages exchanges being pull or push.
 * Those methods are supposed to be executed what ever the result of the exchange as they are in charge
 * of message's state.
 *
 * @author Thomas Dussart
 * @since 3.3
 */

public interface ReliabilityService {
    /**
     * Method supposed to be called after pushing or pulling.
     * It will handle the notifications and increase of messages attempts.
     *
     * @param reliabilityDTO               holding the following values
     *        {@link ReliabilityDTO#getUserMessage}                  the processed {@link UserMessage}.
     *        {@link ReliabilityDTO#getUserMessageLog}               the processed {@link UserMessageLog}.
     *        {@link ReliabilityDTO#getAttempt}                      the {@link MessageAttempt} performed
     *        {@link ReliabilityDTO#getLegConfiguration}             the {@link LegConfiguration} of this message exchange.
     *        {@link ReliabilityDTO#getReliabilityCheck}   the state of the reliability check.
     *        {@link ReliabilityDTO#getRequestRawXMLMessage}         The raw xml message
     *        {@link ReliabilityDTO#getResponseResult}               status result for reliability.
     *        {@link ReliabilityDTO#getResponseSoapMessage}          the Soap Response
     *        {@link ReliabilityDTO#getThrowable}                    the error if applicable
     */
    void handleReliability(ReliabilityDTO reliabilityDTO);

    /**
     * Update the connectivity status of a remote party
     * @param status - the state of connectivity
     * @param partyName - the name of the party
     */
    void updatePartyState(String status, String partyName);

    /**
     * Retrieve the connectivity status of a party
     * @param partyName - the name of the party
     * @return true if party is reachable (the connectivity status = SUCCESS)
     */
    boolean isPartyReachable(String partyName);

    /**
     * Check if the smart retry feature is configured
     * @param partyName
     * @return true if smart retry feature is active
     */
    boolean isSmartRetryEnabledForParty(String partyName);
}
