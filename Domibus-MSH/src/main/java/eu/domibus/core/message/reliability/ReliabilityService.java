package eu.domibus.core.message.reliability;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.core.message.reliability.ReliabilityChecker;
import eu.domibus.ebms3.sender.ResponseResult;

import javax.xml.soap.SOAPMessage;

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
     * @param messageId                  the processed message id.
     * @param reliabilityCheckSuccessful the state of the reliability check.
     * @param responseResult             status result for reliability.
     * @param legConfiguration           the legconfiguration of this message exchange.
     */
    void handleReliability(String messageId, Messaging messaging, UserMessageLog userMessageLog, ReliabilityChecker.CheckResult reliabilityCheckSuccessful, SOAPMessage responseSoapMessage, ResponseResult responseResult, LegConfiguration legConfiguration, MessageAttempt attempt);

    /**
     * This method is used when handleReliability failed
     *
     * @param messageId                  the processed message id.
     * @param reliabilityCheckSuccessful the state of the reliability check.
     * @param responseResult             status result for reliability.
     * @param legConfiguration           the legconfiguration of this message exchange.
     */
    void handleReliabilityInNewTransaction(String messageId, Messaging messaging, UserMessageLog userMessageLog, ReliabilityChecker.CheckResult reliabilityCheckSuccessful, SOAPMessage responseSoapMessage, ResponseResult responseResult, LegConfiguration legConfiguration, MessageAttempt attempt);

}
