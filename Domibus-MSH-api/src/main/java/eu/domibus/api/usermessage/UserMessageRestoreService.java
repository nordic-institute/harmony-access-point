package eu.domibus.api.usermessage;

/**
 * @author Soumya
 * @since 4.2.2
 */
public interface UserMessageRestoreService {

    void restoreFailedMessage(String messageId);
}
