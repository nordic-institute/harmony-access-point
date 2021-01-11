package eu.domibus.api.model;

import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.model.splitandjoin.MessageFragmentEntity;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Entity
@Table(name = "TB_USER_MESSAGE")
public class UserMessage extends AbstractBaseEntity {

    @OneToOne(cascade = CascadeType.ALL)
    protected MessageInfo messageInfo;

    @Embedded
    protected PartyInfo partyInfo; //NOSONAR

    @Embedded
    protected CollaborationInfo collaborationInfo; //NOSONAR

    @Embedded
    protected MessageProperties messageProperties; //NOSONAR

    @Embedded
    protected PayloadInfo payloadInfo; //NOSONAR

    @Column(name = "MPC")
    protected String mpc = Ebms3Constants.DEFAULT_MPC;

    @Column(name = "SPLIT_AND_JOIN")
    protected Boolean splitAndJoin;

    @JoinColumn(name = "FK_MESSAGE_FRAGMENT_ID")
    @OneToOne(cascade = CascadeType.ALL)
    protected MessageFragmentEntity messageFragment;

    @OneToOne(mappedBy = "userMessage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private RawEnvelopeLog rawEnvelopeLog;

    public MessageFragmentEntity getMessageFragment() {
        return messageFragment;
    }

    public void setMessageFragment(MessageFragmentEntity messageFragment) {
        this.messageFragment = messageFragment;
    }

    public MessageInfo getMessageInfo() {
        if (this.messageInfo == null) {
            this.messageInfo = new MessageInfo();
        }
        return this.messageInfo;
    }

    public void setMessageInfo(final MessageInfo value) {
        this.messageInfo = value;
    }

    public PartyInfo getPartyInfo() {
        return this.partyInfo;
    }

    public void setPartyInfo(final PartyInfo value) {
        this.partyInfo = value;
    }

    public CollaborationInfo getCollaborationInfo() {
        return this.collaborationInfo;
    }

    public void setCollaborationInfo(final CollaborationInfo value) {
        this.collaborationInfo = value;
    }

    public MessageProperties getMessageProperties() {
        return this.messageProperties;
    }

    public void setMessageProperties(final MessageProperties value) {
        this.messageProperties = value;
    }

    public PayloadInfo getPayloadInfo() {
        return this.payloadInfo;
    }

    public void setPayloadInfo(final PayloadInfo value) {
        this.payloadInfo = value;
    }

    public String getMpc() {
        return this.mpc;
    }

    public void setMpc(final String value) {
        this.mpc = value;
    }

    public boolean isPayloadOnFileSystem() {
        for (PartInfo partInfo : getPayloadInfo().getPartInfo()) {
            if (StringUtils.isNotEmpty(partInfo.getFileName()))
                return true;
        }
        return false;
    }

    public Boolean isSplitAndJoin() {
        return BooleanUtils.toBoolean(splitAndJoin);
    }

    public void setSplitAndJoin(Boolean splitAndJoin) {
        this.splitAndJoin = splitAndJoin;
    }

    public boolean isUserMessageFragment() {
        return isSplitAndJoin() && messageFragment != null;
    }

    public boolean isSourceMessage() {
        return isSplitAndJoin() && messageFragment == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        UserMessage that = (UserMessage) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(messageInfo, that.messageInfo)
                .append(partyInfo, that.partyInfo)
                .append(collaborationInfo, that.collaborationInfo)
                .append(messageProperties, that.messageProperties)
                .append(payloadInfo, that.payloadInfo)
                .append(mpc, that.mpc)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(messageInfo)
                .append(partyInfo)
                .append(collaborationInfo)
                .append(messageProperties)
                .append(payloadInfo)
                .append(mpc)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("messageInfo", messageInfo)
                .append("partyInfo", partyInfo)
                .append("collaborationInfo", collaborationInfo)
                .append("messageProperties", messageProperties)
                .append("payloadInfo", payloadInfo)
                .append("mpc", mpc)
                .toString();
    }

    public String getFromFirstPartyId() {
        if (getPartyInfo() != null && getPartyInfo().getFrom() != null) {
            return getPartyInfo().getFrom().getFirstPartyId();
        }
        return null;
    }

    public String getToFirstPartyId() {
        if (getPartyInfo() != null && getPartyInfo().getTo() != null) {
            return getPartyInfo().getTo().getFirstPartyId();
        }
        return null;
    }
}
