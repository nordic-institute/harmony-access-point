package eu.domibus.core.message;

import eu.domibus.api.model.splitandjoin.MessageGroupEntity;
import eu.domibus.api.model.UserMessage;

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
