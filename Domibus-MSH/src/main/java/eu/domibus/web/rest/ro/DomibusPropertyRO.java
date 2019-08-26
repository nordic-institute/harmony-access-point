package eu.domibus.web.rest.ro;

import eu.domibus.api.property.DomibusPropertyMetadata;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 *
 * REST resource class for getting the current value of a domibus property along with its metadata
 */

public class DomibusPropertyRO {
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
