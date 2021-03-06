package eu.domibus.core.message.pull;


/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Those states are used when the pull message is locked.
 */
public enum PullMessageState {
    //for the first time.
    FIRST_ATTEMPT,
    //on retry
    RETRY,
    //when the message has exceeded the number of attempts or that maximum delivery date is expired.
    EXPIRED
}
