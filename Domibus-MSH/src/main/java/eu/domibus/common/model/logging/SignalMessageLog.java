package eu.domibus.common.model.logging;

import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.SignalMessage;

import javax.persistence.*;
import java.util.Date;


/**
 * @author Federico Martini
 * @since 3.2
 */
@Entity
@Table(name = "TB_SIGNAL_MESSAGE_LOG")
//@DiscriminatorValue("SIGNAL_MESSAGE")
@NamedQueries({
        @NamedQuery(name = "SignalMessageLog.findByMessageId", query = "select signalMessageLog from SignalMessageLog signalMessageLog where signalMessageLog.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "SignalMessageLog.getMessageStatus", query = "select signalMessageLog.messageStatus from SignalMessageLog signalMessageLog where signalMessageLog.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "SignalMessageLog.setMessageStatus", query = "update SignalMessageLog message set message.messageStatus = :STATUS where message.entityId = :ID"),
        @NamedQuery(name = "SignalMessageLog.findByMessageIdAndRole", query = "select signalMessageLog from SignalMessageLog signalMessageLog where signalMessageLog.messageId=:MESSAGE_ID and signalMessageLog.mshRole=:MSH_ROLE")
})
public class SignalMessageLog extends MessageLog {

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "ID_PK")
    protected SignalMessage signalMessage;

    public SignalMessageLog() {
        setMessageType(MessageType.SIGNAL_MESSAGE);
        setReceived(new Date());
    }

    public void setSignalMessage(SignalMessage signalMessage) {
        this.signalMessage = signalMessage;
    }
}


