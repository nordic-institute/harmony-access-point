package eu.domibus.api.model;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@NamedQueries({
        @NamedQuery(name = "UserMessage.findByGroupEntityId", query = "select mg.sourceMessage from MessageGroupEntity mg where mg.entityId=:ENTITY_ID"),
        @NamedQuery(name = "UserMessage.findByMessageId", query = "select um from UserMessage um where um.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "UserMessage.findByMessageIdAndRole", query = "select um from UserMessage um where um.messageId=:MESSAGE_ID and um.mshRole.role=:MSH_ROLE"),
        @NamedQuery(name = "UserMessage.deleteMessages", query = "delete from UserMessage mi where mi.entityId in :IDS"),
        @NamedQuery(name = "UserMessage.findUserMessageByGroupId",
                query = "select mf.userMessage from MessageFragmentEntity mf where mf.group.groupId = :GROUP_ID order by mf.fragmentNumber asc"),
        @NamedQuery(name = "UserMessage.find",
                query = "select userMessage from UserMessage userMessage where userMessage.messageId IN :MESSAGEIDS"),
        @NamedQuery(name = "UserMessage.findTestMessageFromPartyToPartyDesc",
                query = "select um from UserMessage um " +
                        "where um.testMessage=true and um.mshRole.role=:MSH_ROLE " +
                        "and um.partyInfo.from.fromPartyId.value=:SENDER_PARTY_ID and um.partyInfo.to.toPartyId.value=:PARTY_ID " +
                        "order by um.entityId desc"),
        @NamedQuery(name = "UserMessage.findTestMessageToPartyDesc",
                query = "select um from UserMessage um " +
                        "where um.testMessage=true and um.mshRole.role=:MSH_ROLE " +
                        "and um.partyInfo.to.toPartyId.value=:PARTY_ID " +
                        "order by um.entityId desc"),
        @NamedQuery(name = "UserMessage.findSentTestMessageWithStatusDesc",
                query = "select uml.userMessage from UserMessageLog uml " +
                        "where uml.userMessage.testMessage=true and uml.userMessage.mshRole.role=:MSH_ROLE " +
                        "and uml.userMessage.partyInfo.to.toPartyId.value=:PARTY_ID and uml.messageStatus.messageStatus=:STATUS " +
                        "order by uml.userMessage.entityId desc"),
        @NamedQuery(name = "UserMessage.findTestMessageFromPartyDesc",
                query = "select um from UserMessage um " +
                        "where um.testMessage=true and um.mshRole.role=:MSH_ROLE and um.partyInfo.from.fromPartyId.value=:PARTY_ID " +
                        "order by um.entityId desc"),
})
@NamedNativeQueries({
        @NamedNativeQuery(
                name    =   "UserMessage.findPartitionsForUser_ORACLE",
                query   =   "SELECT partition_name FROM all_tab_partitions WHERE table_owner = :DB_USER and table_name = :TNAME and partition_name <= :PNAME"
        ),
        @NamedNativeQuery(
                name    =   "UserMessage.findPartitions_ORACLE",
                query   =   "SELECT partition_name FROM user_tab_partitions WHERE table_name = :TNAME and partition_name <= :PNAME"
        ),
        @NamedNativeQuery(
                name    =   "UserMessage.verifyPartitionExistsByName",
                query   =   "SELECT count(*) FROM user_tab_partitions WHERE partition_name >= :PNAME"
        )
})
@Entity
@Table(name = UserMessage.TB_USER_MESSAGE)
public class UserMessage extends AbstractBaseEntity {

    public static final String TB_USER_MESSAGE = "TB_USER_MESSAGE";

    public static final String MESSAGE_ID_CONTEXT_PROPERTY = "ebms.messageid";
    public static final String USER_MESSAGE_ID_KEY_CONTEXT_PROPERTY = "USER_MESSAGE_ENTITY_KEY_CONTEXT_PROPERTY";
    public static final String USER_MESSAGE_DUPLICATE_KEY = "USER_MESSAGE_DUPLICATE_KEY_PROPERTY";

    public static final long DEFAULT_USER_MESSAGE_ID_PK = 19700101; // 1st of January 1970

    @Column(name = "MESSAGE_ID", nullable = false, unique = true, updatable = false)
    @NotNull
    protected String messageId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MSH_ROLE_ID_FK")
    private MSHRoleEntity mshRole;

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

    @Column(name = "TEST_MESSAGE")
    protected Boolean testMessage;

    @Embedded
    protected PartyInfo partyInfo; //NOSONAR

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACTION_ID_FK")
    protected ActionEntity action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVICE_ID_FK")
    protected ServiceEntity service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AGREEMENT_ID_FK")
    protected AgreementRefEntity agreementRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MPC_ID_FK")
    protected MpcEntity mpc;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_MESSAGE_PROPERTIES",
            joinColumns = @JoinColumn(name = "USER_MESSAGE_ID_FK"),
            inverseJoinColumns = @JoinColumn(name = "MESSAGE_PROPERTY_FK")
    )
    protected Set<MessageProperty> messageProperties; //NOSONAR

    public boolean isTestMessage() {
        return BooleanUtils.toBoolean(testMessage);
    }

    public void setTestMessage(Boolean testMessage) {
        this.testMessage = testMessage;
    }

    public boolean isSplitAndJoin() {
        return isSourceMessage() || isMessageFragment();
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
        return StringUtils.SPACE.equals(this.conversationId) ? StringUtils.EMPTY : this.conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = StringUtils.EMPTY.equals(conversationId) ? StringUtils.SPACE : conversationId;
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
        if (action == null) {
            return null;
        }
        return action.getValue();
    }

    public void setAction(ActionEntity action) {
        this.action = action;
    }

    public ServiceEntity getService() {
        return service;
    }

    public String getServiceValue() {
        if (service == null) {
            return null;
        }
        return service.getValue();
    }

    public void setService(ServiceEntity service) {
        this.service = service;
    }

    public AgreementRefEntity getAgreementRef() {
        return agreementRef;
    }

    public String getAgreementRefValue() {
        if(agreementRef == null) {
            return null;
        }
        return agreementRef.getValue();
    }

    public void setAgreementRef(AgreementRefEntity agreementRef) {
        this.agreementRef = agreementRef;
    }

    public MpcEntity getMpc() {
        return mpc;
    }

    public String getMpcValue() {
        if (mpc == null) {
            return null;
        }
        return mpc.getValue();
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

    public boolean isSourceMessage() {
        return BooleanUtils.toBoolean(sourceMessage);
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

    public MSHRoleEntity getMshRole() {
        return mshRole;
    }

    public void setMshRole(MSHRoleEntity mshRole) {
        this.mshRole = mshRole;
    }

    @Override
    public String toString() {
        return "UserMessage{" +
                "messageId='" + messageId + '\'' +
                ", mshRole='" + (mshRole == null ? null : mshRole.getRole()) + '\'' +
                ", refToMessageId='" + refToMessageId + '\'' +
                ", conversationId='" + conversationId + '\'' +
                ", timestamp=" + timestamp +
                ", sourceMessage=" + sourceMessage +
                ", messageFragment=" + messageFragment +
                ", testMessage=" + testMessage +
                '}';
    }
}
