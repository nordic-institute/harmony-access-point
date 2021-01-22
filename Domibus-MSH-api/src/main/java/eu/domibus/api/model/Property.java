package eu.domibus.api.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.regex.Pattern;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Entity
@Table(name = "TB_PROPERTY")
@NamedQuery(name = "Property.findPropertiesByMessageId",
        query = "select property                                           " +
                "from UserMessage userMessage, Property property           " +
                "where userMessage.messageInfo.messageId = :MSG_ID         " +
                " AND userMessage.entityId = property.userMessage.entityId ")
public class Property extends AbstractBaseEntity implements Comparable<Property> {

    public static final String MIME_TYPE = "MimeType";
    public static final String CHARSET = "CharacterSet";
    public static final Pattern CHARSET_PATTERN = Pattern.compile("[A-Za-z]([A-Za-z0-9._-])*");
    public static final int VALUE_MAX_SIZE = 1024;

    @Column(name = "VALUE")
    protected String value;

    @Column(name = "NAME", nullable = false)
    protected String name;

    @Column(name = "TYPE", nullable = true)
    protected String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MESSAGEPROPERTIES_ID")
    protected UserMessage userMessage;

    /**
     * Gets the value of the value property.
     *
     * @return possible object is {@link String }
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value allowed object is {@link String }
     */
    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String }
     */
    public void setName(final String value) {
        this.name = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Property property = (Property) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(value, property.value)
                .append(name, property.name)
                .append(type, property.type)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(value)
                .append(name)
                .append(type)
                .toHashCode();
    }

    @Override
    public int compareTo(final Property o) {
        return this.hashCode() - o.hashCode();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UserMessage getUserMessage() {
        return userMessage;
    }

    public Property setUserMessage(UserMessage userMessage) {
        this.userMessage = userMessage;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("value", value)
                .append("name", name)
                .append("type", type)
                .toString();
    }
}
