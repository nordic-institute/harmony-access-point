package eu.domibus.api.model;

import javax.persistence.*;
import java.util.List;


/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Entity
@Table(name = "TB_MESSAGING")
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
