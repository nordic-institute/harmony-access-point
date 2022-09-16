package eu.domibus.api.usermessage;

import eu.domibus.api.model.MSHRole;

/** This event is generated when a message is downloaded in the DatabaseMessageHandler and caught by the DomibusRollbackListener
 * @author idragusa
 * @since 5.0
 */
public class UserMessageDownloadEvent {

    protected String messageId;

    protected String mshRole;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMshRole() {
        return mshRole;
    }

    public void setMshRole(String mshRole) {
        this.mshRole = mshRole;
    }
}
