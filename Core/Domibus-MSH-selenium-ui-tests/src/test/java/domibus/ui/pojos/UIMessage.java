package domibus.ui.pojos;

public class UIMessage {
	
	private String messageId;
	private String conversationId;
	private String fromPartyId;
	private String toPartyId;
	private String messageStatus;
	private String notificationStatus;
	private String mshRole;
	private String messageType;
	private Long deleted;
	private Long received;
	private Integer sendAttempts;
	private Integer sendAttemptsMax;
	private Long nextAttempt;
	private String originalSender;
	private String finalRecipient;
	private String refToMessageId;
	private Long failed;
	private Long restored;
	private String messageSubtype;
	private Boolean messageFragment;
	private Boolean sourceMessage;
	
	@Override
	public String toString() {
		return "UIMessage{" +
				"messageId='" + messageId + '\'' +
				", conversationId='" + conversationId + '\'' +
				", fromPartyId='" + fromPartyId + '\'' +
				", toPartyId='" + toPartyId + '\'' +
				", messageStatus='" + messageStatus + '\'' +
				", notificationStatus='" + notificationStatus + '\'' +
				", mshRole='" + mshRole + '\'' +
				", messageType='" + messageType + '\'' +
				", deleted=" + deleted +
				", received=" + received +
				", sendAttempts=" + sendAttempts +
				", sendAttemptsMax=" + sendAttemptsMax +
				", nextAttempt=" + nextAttempt +
				", originalSender='" + originalSender + '\'' +
				", finalRecipient='" + finalRecipient + '\'' +
				", refToMessageId='" + refToMessageId + '\'' +
				", failed=" + failed +
				", restored=" + restored +
				", messageSubtype='" + messageSubtype + '\'' +
				", messageFragment=" + messageFragment +
				", sourceMessage=" + sourceMessage +
				'}';
	}
	
	public String getMessageId() {
		return messageId;
	}
	
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	
	public String getConversationId() {
		return conversationId;
	}
	
	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}
	
	public String getFromPartyId() {
		return fromPartyId;
	}
	
	public void setFromPartyId(String fromPartyId) {
		this.fromPartyId = fromPartyId;
	}
	
	public String getToPartyId() {
		return toPartyId;
	}
	
	public void setToPartyId(String toPartyId) {
		this.toPartyId = toPartyId;
	}
	
	public String getMessageStatus() {
		return messageStatus;
	}
	
	public void setMessageStatus(String messageStatus) {
		this.messageStatus = messageStatus;
	}
	
	public String getNotificationStatus() {
		return notificationStatus;
	}
	
	public void setNotificationStatus(String notificationStatus) {
		this.notificationStatus = notificationStatus;
	}
	
	public String getMshRole() {
		return mshRole;
	}
	
	public void setMshRole(String mshRole) {
		this.mshRole = mshRole;
	}
	
	public String getMessageType() {
		return messageType;
	}
	
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	
	public Long getDeleted() {
		return deleted;
	}
	
	public void setDeleted(Long deleted) {
		this.deleted = deleted;
	}
	
	public Long getReceived() {
		return received;
	}
	
	public void setReceived(Long received) {
		this.received = received;
	}
	
	public Integer getSendAttempts() {
		return sendAttempts;
	}
	
	public void setSendAttempts(Integer sendAttempts) {
		this.sendAttempts = sendAttempts;
	}
	
	public Integer getSendAttemptsMax() {
		return sendAttemptsMax;
	}
	
	public void setSendAttemptsMax(Integer sendAttemptsMax) {
		this.sendAttemptsMax = sendAttemptsMax;
	}
	
	public Long getNextAttempt() {
		return nextAttempt;
	}
	
	public void setNextAttempt(Long nextAttempt) {
		this.nextAttempt = nextAttempt;
	}
	
	public String getOriginalSender() {
		return originalSender;
	}
	
	public void setOriginalSender(String originalSender) {
		this.originalSender = originalSender;
	}
	
	public String getFinalRecipient() {
		return finalRecipient;
	}
	
	public void setFinalRecipient(String finalRecipient) {
		this.finalRecipient = finalRecipient;
	}
	
	public String getRefToMessageId() {
		return refToMessageId;
	}
	
	public void setRefToMessageId(String refToMessageId) {
		this.refToMessageId = refToMessageId;
	}
	
	public Long getFailed() {
		return failed;
	}
	
	public void setFailed(Long failed) {
		this.failed = failed;
	}
	
	public Long getRestored() {
		return restored;
	}
	
	public void setRestored(Long restored) {
		this.restored = restored;
	}
	
	public String getMessageSubtype() {
		return messageSubtype;
	}
	
	public void setMessageSubtype(String messageSubtype) {
		this.messageSubtype = messageSubtype;
	}
	
	public Boolean getMessageFragment() {
		return messageFragment;
	}
	
	public void setMessageFragment(Boolean messageFragment) {
		this.messageFragment = messageFragment;
	}
	
	public Boolean getSourceMessage() {
		return sourceMessage;
	}
	
	public void setSourceMessage(Boolean sourceMessage) {
		this.sourceMessage = sourceMessage;
	}
}
