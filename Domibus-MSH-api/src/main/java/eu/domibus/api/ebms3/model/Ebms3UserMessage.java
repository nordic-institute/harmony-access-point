package eu.domibus.api.ebms3.model;

import eu.domibus.api.ebms3.Ebms3Constants;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.xml.bind.annotation.*;

/**
 * This element has the following attributes:
 * • eb:Messaging/eb:UserMessage/@mpc: This OPTIONAL attribute contains a URI that
 * identifies the Message Partition Channel to which the message is assigned. The absence of this
 * element indicates the use of the default MPC. When the message is pulled, the value of this
 * attribute MUST indicate the MPC requested in the PullRequest message.
 * This element has the following children elements:
 * • eb:Messaging/eb:UserMessage/eb:MessageInfo: This REQUIRED element occurs once,
 * and contains data that identifies the message, and relates to other messages' identifiers.
 * • eb:Messaging/eb:UserMessage/eb:PartyInfo: This REQUIRED element occurs once,
 * and contains data about originating party and destination party.
 * • eb:Messaging/eb:UserMessage/eb:CollaborationInfo: This REQUIRED element
 * occurs once, and contains elements that facilitate collaboration between parties.
 * • eb:Messaging/eb:UserMessage/eb:MessageProperties: This OPTIONAL element
 * occurs at most once, and contains message properties that are user-specific. As parts of the
 * header such properties allow for more efficient monitoring, correlating, dispatching and validating
 * functions (even if these are out of scope of ebMS specification) which would otherwise require
 * payload access.
 * • eb:Messaging/eb:UserMessage/eb:PayloadInfo: This OPTIONAL element occurs at
 * most once, and identifies payload data associated with the message, whether included as part of
 * the message as payload document(s) contained in a Payload Container, or remote resources
 * accessible via a URL. The purpose of the PayloadInfo is (a) to make it easier to directly extract a
 * particular payload associated with this User message, (b) to allow an application to determine
 * whether it can process the payload without having to parse it.
 *
 * @author Christian Koch
 * @version 1.0
 * @since 3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserMessage",
        propOrder = {"messageInfo", "partyInfo", "collaborationInfo", "messageProperties", "payloadInfo"})
public class Ebms3UserMessage {

    @XmlElement(name = "MessageInfo", required = true)
    protected Ebms3MessageInfo messageInfo;

    @XmlElement(name = "PartyInfo", required = true)
    protected Ebms3PartyInfo partyInfo; //NOSONAR

    @XmlElement(name = "CollaborationInfo", required = true)
    protected Ebms3CollaborationInfo collaborationInfo; //NOSONAR

    @XmlElement(name = "MessageProperties")
    protected Ebms3MessageProperties messageProperties; //NOSONAR

    @XmlElement(name = "PayloadInfo")
    protected Ebms3PayloadInfo payloadInfo; //NOSONAR

    @XmlAttribute(name = "mpc")
    @XmlSchemaType(name = "anyURI")
    protected String mpc = Ebms3Constants.DEFAULT_MPC;

    /**
     * This REQUIRED element occurs once,
     * and contains data that identifies the message, and relates to other messages' identifiers.
     *
     * @return possible object is {@link Ebms3MessageInfo }
     */
    public Ebms3MessageInfo getMessageInfo() {
        if (this.messageInfo == null) {
            this.messageInfo = new Ebms3MessageInfo();
        }
        return this.messageInfo;
    }

    /**
     * This REQUIRED element occurs once,
     * and contains data that identifies the message, and relates to other messages' identifiers.
     *
     * @param value allowed object is {@link Ebms3MessageInfo }
     */
    public void setMessageInfo(final Ebms3MessageInfo value) {
        this.messageInfo = value;
    }

    /**
     * This REQUIRED element occurs once,
     * and contains data about originating party and destination party.
     *
     * @return possible object is {@link Ebms3PartyInfo }
     */
    public Ebms3PartyInfo getPartyInfo() {
        return this.partyInfo;
    }

    /**
     * This REQUIRED element occurs once,
     * and contains data about originating party and destination party.
     *
     * @param value allowed object is {@link Ebms3PartyInfo }
     */
    public void setPartyInfo(final Ebms3PartyInfo value) {
        this.partyInfo = value;
    }

    /**
     * This REQUIRED element
     * occurs once, and contains elements that facilitate collaboration between parties.
     *
     * @return possible object is {@link Ebms3CollaborationInfo }
     */
    public Ebms3CollaborationInfo getCollaborationInfo() {
        return this.collaborationInfo;
    }

    /**
     * This REQUIRED element
     * occurs once, and contains elements that facilitate collaboration between parties.
     *
     * @param value allowed object is {@link Ebms3CollaborationInfo }
     */
    public void setCollaborationInfo(final Ebms3CollaborationInfo value) {
        this.collaborationInfo = value;
    }

    /**
     * This OPTIONAL element
     * occurs at most once, and contains message properties that are user-specific. As parts of the
     * header such properties allow for more efficient monitoring, correlating, dispatching and validating
     * functions (even if these are out of scope of ebMS specification) which would otherwise require
     * payload access.
     *
     * @return possible object is {@link Ebms3MessageProperties }
     */
    public Ebms3MessageProperties getMessageProperties() {
        return this.messageProperties;
    }

    /**
     * This OPTIONAL element
     * occurs at most once, and contains message properties that are user-specific. As parts of the
     * header such properties allow for more efficient monitoring, correlating, dispatching and validating
     * functions (even if these are out of scope of ebMS specification) which would otherwise require
     * payload access.
     *
     * @param value allowed object is {@link Ebms3MessageProperties }
     */
    public void setMessageProperties(final Ebms3MessageProperties value) {
        this.messageProperties = value;
    }

    /**
     * This OPTIONAL element occurs at
     * most once, and identifies payload data associated with the message, whether included as part of
     * the message as payload document(s) contained in a Payload Container, or remote resources
     * accessible via a URL. The purpose of the PayloadInfo is (a) to make it easier to directly extract a
     * particular payload associated with this User message, (b) to allow an application to determine
     * whether it can process the payload without having to parse it.
     *
     * @return possible object is {@link Ebms3PayloadInfo }
     */
    public Ebms3PayloadInfo getPayloadInfo() {
        return this.payloadInfo;
    }

    /**
     * This OPTIONAL element occurs at
     * most once, and identifies payload data associated with the message, whether included as part of
     * the message as payload document(s) contained in a Payload Container, or remote resources
     * accessible via a URL. The purpose of the PayloadInfo is (a) to make it easier to directly extract a
     * particular payload associated with this User message, (b) to allow an application to determine
     * whether it can process the payload without having to parse it.
     *
     * @param value allowed object is {@link Ebms3PayloadInfo }
     */
    public void setPayloadInfo(final Ebms3PayloadInfo value) {
        this.payloadInfo = value;
    }

    /**
     * This OPTIONAL attribute contains a URI that
     * identifies the Message Partition Channel to which the message is assigned. The absence of this
     * element indicates the use of the default MPC. When the message is pulled, the value of this
     * attribute MUST indicate the MPC requested in the PullRequest message.
     *
     * @return possible object is {@link String }
     */
    public String getMpc() {
        return this.mpc;
    }

    /**
     * This OPTIONAL attribute contains a URI that
     * identifies the Message Partition Channel to which the message is assigned. The absence of this
     * element indicates the use of the default MPC. When the message is pulled, the value of this
     * attribute MUST indicate the MPC requested in the PullRequest message.
     *
     * @param value allowed object is {@link String }
     */
    public void setMpc(final String value) {
        this.mpc = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Ebms3UserMessage that = (Ebms3UserMessage) o;

        return new EqualsBuilder()
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
