package eu.domibus.api.model.splitandjoin;

import eu.domibus.api.model.AbstractBaseEntity;
import eu.domibus.api.model.MSHRoleEntity;
import eu.domibus.api.model.UserMessage;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.math.BigInteger;

/**
 * Entity class for storing message fragment group details. For more details about relations to other entities please check the SplitAndJoin specs.
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
@Entity
@Table(name = "TB_SJ_MESSAGE_GROUP")
@NamedQueries({
        @NamedQuery(name = "MessageGroupEntity.findByUserMessageEntityId", query = "SELECT gr FROM MessageFragmentEntity frag join frag.group gr where frag.entityId= :USER_MESSAGE_ENTITY_ID"),
        @NamedQuery(name = "MessageGroupEntity.findByGroupId", query = "SELECT c FROM MessageGroupEntity c where c.groupId=:GROUP_ID"),
        @NamedQuery(name = "MessageGroupEntity.findReceivedNonExpiredOrRejected", query = "SELECT c FROM MessageGroupEntity c where c.mshRole = :MSH_ROLE " +
                "and c.fragmentCount <> c.receivedFragments and ( (c.rejected is null or c.rejected=false) or (c.expired is null or c.expired=false) )"),
        @NamedQuery(name = "MessageGroupEntity.findSendNonExpiredOrRejected", query = "SELECT c FROM MessageGroupEntity c, UserMessageLog msg join msg.userMessage um where c.mshRole = :MSH_ROLE " +
                " and ( (c.rejected is null or c.rejected=false) or (c.expired is null or c.expired=false) ) and c.sourceMessage.entityId = um.entityId and msg.messageStatus = :SOURCE_MSG_STATUS")
})
public class MessageGroupEntity extends AbstractBaseEntity {

    @Column(name = "GROUP_ID")
    protected String groupId;

    @Column(name = "MESSAGE_SIZE")
    protected BigInteger messageSize;

    @Column(name = "FRAGMENT_COUNT")
    protected Long fragmentCount;

    @Column(name = "SENT_FRAGMENTS")
    protected Long sentFragments = 0L;

    @Column(name = "RECEIVED_FRAGMENTS")
    protected Long receivedFragments = 0L;

    @Column(name = "COMPRESSION_ALGORITHM")
    protected String compressionAlgorithm;

    @Column(name = "COMPRESSED_MESSAGE_SIZE")
    protected BigInteger compressedMessageSize;

    @Column(name = "SOAP_ACTION")
    protected String soapAction;

    @Column(name = "REJECTED")
    protected Boolean rejected;

    @Column(name = "EXPIRED")
    protected Boolean expired;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "MSH_ROLE_ID_FK")
    private MSHRoleEntity mshRole;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PK")
    @MapsId
    protected MessageHeaderEntity messageHeaderEntity;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SOURCE_MESSAGE_ID_FK")
    protected UserMessage sourceMessage;

    public void incrementSentFragments() {
        if (sentFragments == null) {
            sentFragments = 0L;
        }
        sentFragments++;
    }

    public void incrementReceivedFragments() {
        if (receivedFragments == null) {
            receivedFragments = 0L;
        }
        receivedFragments++;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public BigInteger getMessageSize() {
        return messageSize;
    }

    public void setMessageSize(BigInteger messageSize) {
        this.messageSize = messageSize;
    }

    public Long getFragmentCount() {
        return fragmentCount;
    }

    public void setFragmentCount(Long fragmentCount) {
        this.fragmentCount = fragmentCount;
    }

    public Long getSentFragments() {
        return sentFragments;
    }

    public void setSentFragments(Long sentFragments) {
        this.sentFragments = sentFragments;
    }

    public Long getReceivedFragments() {
        return receivedFragments;
    }

    public void setReceivedFragments(Long receivedFragments) {
        this.receivedFragments = receivedFragments;
    }

    public String getCompressionAlgorithm() {
        return compressionAlgorithm;
    }

    public void setCompressionAlgorithm(String compressionAlgorithm) {
        this.compressionAlgorithm = compressionAlgorithm;
    }

    public BigInteger getCompressedMessageSize() {
        return compressedMessageSize;
    }

    public void setCompressedMessageSize(BigInteger compressedMessageSize) {
        this.compressedMessageSize = compressedMessageSize;
    }

    public String getSoapAction() {
        return soapAction;
    }

    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }

    public Boolean getRejected() {
        return rejected;
    }

    public void setRejected(Boolean rejected) {
        this.rejected = rejected;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public MSHRoleEntity getMshRole() {
        return mshRole;
    }

    public void setMshRole(MSHRoleEntity mshRole) {
        this.mshRole = mshRole;
    }

    public MessageHeaderEntity getMessageHeaderEntity() {
        return messageHeaderEntity;
    }

    public void setMessageHeaderEntity(MessageHeaderEntity messageHeaderEntity) {
        this.messageHeaderEntity = messageHeaderEntity;
    }

    public UserMessage getSourceMessage() {
        return sourceMessage;
    }

    public void setSourceMessage(UserMessage sourceMessage) {
        this.sourceMessage = sourceMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessageGroupEntity that = (MessageGroupEntity) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(groupId, that.groupId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(groupId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("groupId", groupId)
                .append("messageSize", messageSize)
                .append("fragmentCount", fragmentCount)
                .append("sentFragments", sentFragments)
                .append("receivedFragments", receivedFragments)
                .append("compressionAlgorithm", compressionAlgorithm)
                .append("compressedMessageSize", compressedMessageSize)
                .append("soapAction", soapAction)
                .append("rejected", rejected)
                .append("expired", expired)
                .toString();
    }
}
