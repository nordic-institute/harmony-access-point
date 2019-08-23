package eu.domibus.api.property;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 */
public class DomibusProperty {
    private String value;

    private DomibusPropertyMetadata metadata;

    public DomibusPropertyMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(DomibusPropertyMetadata metadata) {
        this.metadata = metadata;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
