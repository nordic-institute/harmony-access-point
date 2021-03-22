package eu.domibus.core.message;

import eu.domibus.core.message.splitandjoin.MessageGroupEntity;
import eu.domibus.ebms3.common.model.UserMessage;

/**
 * Defines the contract for creating UserMessages or UserMessageFragments
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface UserMessageFactory {

    UserMessage createUserMessageFragment(UserMessage sourceMessage, MessageGroupEntity messageGroupEntity, Long fragmentNumber, String fragmentFile);

    UserMessage cloneUserMessageFragment(UserMessage sourceMessage);

}
