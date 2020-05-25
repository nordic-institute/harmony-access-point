package eu.domibus.web.rest.ro;

/**
 * Holds the domibus property metadata type name and regular expression used for validation
 * @author Ion Perpegel
 * @since 4.2
 */
public class DomibusPropertyTypeRO {

    private String name;

    private String regularExpression;

    public DomibusPropertyTypeRO(String name, String regularExpression) {
        this.name = name;
        this.regularExpression = regularExpression;
    }

    public String getName() {
        return name;
    }

    public String getRegularExpression() {
        return regularExpression;
    }

}
