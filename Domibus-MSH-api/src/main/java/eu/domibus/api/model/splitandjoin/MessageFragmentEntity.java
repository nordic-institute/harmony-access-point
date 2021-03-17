package eu.domibus.api.model.splitandjoin;

import eu.domibus.api.model.AbstractNoGeneratedPkEntity;
import eu.domibus.api.model.UserMessage;

import javax.persistence.*;

/**
 * Entity class for storing message fragments
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
@Entity
@Table(name = "TB_SJ_MESSAGE_FRAGMENT")
public class MessageFragmentEntity extends AbstractNoGeneratedPkEntity {

    @Column(name = "FRAGMENT_NUMBER")
    protected Long fragmentNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private UserMessage userMessage;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "GROUP_ID_FK")
    protected MessageGroupEntity group;

    public Long getFragmentNumber() {
        return fragmentNumber;
    }

    public void setFragmentNumber(Long fragmentNumber) {
        this.fragmentNumber = fragmentNumber;
    }

    public MessageGroupEntity getGroup() {
        return group;
    }

    public void setGroup(MessageGroupEntity group) {
        this.group = group;
    }

    public UserMessage getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(UserMessage userMessage) {
        this.userMessage = userMessage;
    }
}
