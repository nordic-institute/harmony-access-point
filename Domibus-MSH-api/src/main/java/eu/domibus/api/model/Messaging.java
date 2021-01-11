package eu.domibus.api.model;

import javax.persistence.*;


/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Entity
@Table(name = "TB_MESSAGING")
@NamedQueries({
        @NamedQuery(name = "Messaging.findUserMessageByGroupId",
                query = "select messaging.userMessage from Messaging messaging where messaging.userMessage.messageFragment.groupId = :GROUP_ID order by messaging.userMessage.messageFragment.fragmentNumber asc"),
        @NamedQuery(name = "Messaging.findUserMessageByMessageId",
                query = "select messaging.userMessage from Messaging messaging where messaging.userMessage.messageInfo.messageId = :MESSAGE_ID"),
        @NamedQuery(name = "Messaging.findMessageByMessageId",
                query = "select messaging from Messaging messaging where messaging.userMessage.messageInfo.messageId = :MESSAGE_ID"),
        @NamedQuery(name = "Messaging.findSignalMessageByMessageId",
                query = "select messaging.signalMessage from Messaging messaging where messaging.signalMessage.messageInfo.messageId = :MESSAGE_ID"),

        @NamedQuery(name = "Messaging.findPartInfosForMessage", query = "select m.userMessage.payloadInfo.partInfo from Messaging m where m.userMessage.messageInfo.messageId = :MESSAGE_ID"),

        @NamedQuery(name = "Messaging.emptyPayloads", query = "update PartInfo p set p.binaryData = null where p in :PARTINFOS"),
})
public class Messaging extends AbstractBaseEntity {

    @Column(name = "ID")
    protected String id;

    @JoinColumn(name = "SIGNAL_MESSAGE_ID")
    @OneToOne(cascade = CascadeType.ALL)
    protected SignalMessage signalMessage;

    @JoinColumn(name = "USER_MESSAGE_ID")
    @OneToOne(cascade = CascadeType.ALL)
    protected UserMessage userMessage;

    public SignalMessage getSignalMessage() {
        return this.signalMessage;
    }

    public void setSignalMessage(final SignalMessage signalMessage) {
        this.signalMessage = signalMessage;
    }

    public UserMessage getUserMessage() {
        return this.userMessage;
    }

    public void setUserMessage(final UserMessage userMessage) {
        this.userMessage = userMessage;
    }

    public String getId() {
        return this.id;
    }

    public void setId(final String value) {
        this.id = value;
    }

}
