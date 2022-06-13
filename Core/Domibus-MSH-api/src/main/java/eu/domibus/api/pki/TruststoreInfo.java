package eu.domibus.api.pki;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class TruststoreInfo {
    private String name;

    private String type;

    private String password;

    protected byte[] content;

    public String getName() {
        return name;
    }

    public void setName(String type) {
        this.name = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        TruststoreInfo truststore = (TruststoreInfo) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(name, truststore.name)
                .append(type, truststore.type)
                .append(password, truststore.password)
                .append(content, truststore.content)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(name)
                .append(type)
                .append(password)
                .append(content)
                .toHashCode();
    }
}
