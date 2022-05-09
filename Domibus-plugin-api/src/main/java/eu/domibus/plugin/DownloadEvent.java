package eu.domibus.plugin;

/** This event is generated when a message is downloaded in the DatabaseMessageHandler and caught by
 * the DomibusRollbackListener
 * @author idragusa
 * @since Domibus 5.0
 */
public class DownloadEvent {

    protected String messageId;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
