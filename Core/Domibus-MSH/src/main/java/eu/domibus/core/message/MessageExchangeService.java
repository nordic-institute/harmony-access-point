package eu.domibus.core.message;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatusEntity;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.api.model.RawEnvelopeDto;
import eu.domibus.core.message.pull.MpcService;
import eu.domibus.core.message.pull.PullContext;
import eu.domibus.api.model.UserMessage;
import eu.domibus.plugin.ProcessingType;

/**
 * @author Thomas Dussart
 * @since 3.3
 * Service returning information about the message exchange.
 */

public interface MessageExchangeService {

    /**
     * This method with analyse the messageExchange in order to find if the message should be pushed of pulled.
     * The status will be set in messageExchangeContext.
     *
     * @param messageExchangeConfiguration the message configuration used to retrieve the associated process.
     * @param processingType
     * @return the status of the message.
     */
    MessageStatusEntity getMessageStatus(final MessageExchangeConfiguration messageExchangeConfiguration, ProcessingType processingType);

    /**
     * @return SEND_ENQUEUED.
     */
    MessageStatusEntity getMessageStatusForPush();

    /**
     * Failed messages have the same final status (SEND_FAILED) being for a pushed or a pulled message.
     * So when we do restore and resend a message there is the need to know which kind of message it was
     * originally, in order to restore it properly.
     *
     * @param messageId the message id.
     * @param role
     * @return the status the message should be put back to.
     */
    MessageStatusEntity retrieveMessageRestoreStatus(String messageId, MSHRole role);

    /**
     * Load pmode and find pull process in order to initialize pull request.
     */
    void initiatePullRequest();

    /**
     * Load pmode and find pull process in order to initialize pull request.
     *
     * @param mpc the mpc of the exchange
     */
    void initiatePullRequest(final String mpc);

    /**
     * Check if a message exist for the association mpc/responder. If it does it returns the first one that arrived.
     *
     * @param mpc       the mpc contained in the pull request.
     * @param initiator the party for who this message is related.
     * @return a UserMessage id  if found.
     */
    String retrieveReadyToPullUserMessageId(String mpc, Party initiator);

    /**
     * When a pull request comes in, there is very litle information.  From this information we retrieve
     * the initiator, the responder and the pull process leg configuration from wich we can retrieve security information
     *
     * @param mpcQualifiedName the mpc attribute within the pull request.
     * @return a pullcontext with all the information needed to continue with the pull process.
     */
    PullContext extractProcessOnMpc(String mpcQualifiedName);

    /**
     * In case of a pull message, the output soap envelope needs to be saved in order to be saved in order to check the
     * non repudiation.
     *  @param rawXml    the soap envelope
     * @param messageId the user message
     * @param mshRole
     */

    void saveRawXml(String rawXml, String messageId, MSHRole mshRole);

    /**
     * Retrieve the unique raw message of UserMessage. Enforce that it is unique.
     *
     * @param messageId the id of the message.
     * @return the raw soap envelop.
     */
    RawEnvelopeDto findPulledMessageRawXmlByMessageId(String messageId);

    void verifyReceiverCertificate(final LegConfiguration legConfiguration, String receiverName);

    void verifySenderCertificate(LegConfiguration legConfiguration, String receiverName);

    /**
     * See {@link MpcService#forcePullOnMpc(String)}
     */
    boolean forcePullOnMpc(String mpc);

    /**
     * See {@link MpcService#forcePullOnMpc(UserMessage)}
     */
    boolean forcePullOnMpc(UserMessage userMessage);

    /**
     * See {@link MpcService#extractInitiator(String)}
     */
    String extractInitiator(String mpc);

    /**
     * See {@link MpcService#extractBaseMpc(String)}
     */
    String extractBaseMpc(String mpc);
}
