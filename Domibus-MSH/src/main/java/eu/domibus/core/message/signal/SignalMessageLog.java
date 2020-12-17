package eu.domibus.core.message.signal;

import eu.domibus.core.message.MessageLog;
import eu.domibus.ebms3.common.model.MessageType;

import javax.persistence.*;
import java.util.Date;


/**
 * @author Federico Martini
 * @since 3.2
 */
@Entity
@Table(name = "TB_MESSAGE_LOG")
@DiscriminatorValue("SIGNAL_MESSAGE")
@NamedQueries({
        @NamedQuery(name = "SignalMessageLog.findByMessageId", query = "select signalMessageLog from SignalMessageLog signalMessageLog where signalMessageLog.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "SignalMessageLog.getMessageStatus", query = "select signalMessageLog.messageStatus from SignalMessageLog signalMessageLog where signalMessageLog.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "SignalMessageLog.findByMessageIdAndRole", query = "select signalMessageLog from SignalMessageLog signalMessageLog where signalMessageLog.messageId=:MESSAGE_ID and signalMessageLog.mshRole=:MSH_ROLE"),
        @NamedQuery(name = "SignalMessageLog.deleteMessageLogs", query = "delete from SignalMessageLog sml where sml.messageId in :MESSAGEIDS"),
})
public class SignalMessageLog extends MessageLog {

    public SignalMessageLog() {
        setMessageType(MessageType.SIGNAL_MESSAGE);
        setReceived(new Date());
    }


}


