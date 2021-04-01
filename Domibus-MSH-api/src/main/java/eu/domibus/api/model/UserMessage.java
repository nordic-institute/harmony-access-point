package eu.domibus.api.model;

import org.apache.commons.lang3.BooleanUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@NamedQueries({
        @NamedQuery(name = "UserMessage.findByGroupEntityId", query = "select mg.sourceMessage from MessageGroupEntity mg where mg.entityId=:ENTITY_ID"),
        @NamedQuery(name = "UserMessage.findByMessageId", query = "select um from UserMessage um where um.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessage.deleteMessages", query = "delete from UserMessage mi where mi.messageId in :MESSAGEIDS"),
        @NamedQuery(name = "UserMessage.findUserMessageByGroupId",
                query = "select mf.userMessage from MessageFragmentEntity mf where mf.group.groupId = :GROUP_ID order by mf.fragmentNumber asc"),
})
@Entity
@Table(name = "TB_USER_MESSAGE")
public class UserMessage extends AbstractBaseEntity {

    public static final String MESSAGE_ID_CONTEXT_PROPERTY = "ebms.messageid";

    @Column(name = "MESSAGE_ID", nullable = false, unique = true, updatable = false)
    @NotNull
    protected String messageId;

    @Column(name = "REF_TO_MESSAGE_ID")
    protected String refToMessageId;

    @Column(name = "CONVERSATION_ID", nullable = false)
    @NotNull
    protected String conversationId;

    @Column(name = "EBMS3_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @Column(name = "SOURCE_MESSAGE")
    protected Boolean sourceMessage;

    @Column(name = "MESSAGE_FRAGMENT")
    protected Boolean messageFragment;

    @Embedded
    protected PartyInfo partyInfo; //NOSONAR

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ACTION_ID_FK")
    protected ActionEntity action;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVICE_ID_FK")
    protected ServiceEntity service;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AGREEMENT_ID_FK")
    protected AgreementRef agreementRef;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "MPC_ID_FK")
    protected MpcEntity mpc;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(name = "TB_MESSAGE_PROPERTIES",
            joinColumns = @JoinColumn(name = "USER_MESSAGE_ID_FK"),
            inverseJoinColumns = @JoinColumn(name = "MESSAGE_PROPERTY_FK")
    )
    protected Set<MessageProperty> messageProperties; //NOSONAR

    @Transient
    protected List<PartInfo> partInfoList;

    public boolean isSplitAndJoin() {
        return sourceMessage || messageFragment;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getRefToMessageId() {
        return refToMessageId;
    }

    public void setRefToMessageId(String refToMessageId) {
        this.refToMessageId = refToMessageId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public PartyInfo getPartyInfo() {
        return partyInfo;
    }

    public void setPartyInfo(PartyInfo partyInfo) {
        this.partyInfo = partyInfo;
    }

    public ActionEntity getAction() {
        return action;
    }

    public String getActionValue() {
        return action.getValue();
    }

    public void setAction(ActionEntity action) {
        this.action = action;
    }

    public ServiceEntity getService() {
        return service;
    }

    public void setService(ServiceEntity service) {
        this.service = service;
    }

    public AgreementRef getAgreementRef() {
        return agreementRef;
    }

    public void setAgreementRef(AgreementRef agreementRef) {
        this.agreementRef = agreementRef;
    }

    public MpcEntity getMpc() {
        return mpc;
    }

    public void setMpc(MpcEntity mpc) {
        this.mpc = mpc;
    }

    public Set<MessageProperty> getMessageProperties() {
        return messageProperties;
    }

    public void setMessageProperties(Set<MessageProperty> messageProperties) {
        this.messageProperties = messageProperties;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean isSourceMessage() {
        return sourceMessage;
    }

    public void setSourceMessage(Boolean sourceMessage) {
        this.sourceMessage = sourceMessage;
    }

    public boolean isMessageFragment() {
        return BooleanUtils.toBoolean(messageFragment);
    }

    public void setMessageFragment(Boolean messageFragment) {
        this.messageFragment = messageFragment;
    }

    public List<PartInfo> getPartInfoList() {
        return partInfoList;
    }

    public void setPartInfoList(List<PartInfo> partInfoList) {
        this.partInfoList = partInfoList;
    }
}
