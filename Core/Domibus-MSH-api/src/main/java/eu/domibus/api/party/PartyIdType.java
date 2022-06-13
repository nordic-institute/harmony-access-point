package eu.domibus.api.party;

public class PartyIdType {

    protected String name;

    protected String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "PartyIdType{" + "name=" + name + ", value=" + value + '}';
    }

}
