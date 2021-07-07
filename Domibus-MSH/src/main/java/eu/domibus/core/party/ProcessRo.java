package eu.domibus.core.party;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class ProcessRo {

    private String entityId;

    private String name;

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProcessRo processRo = (ProcessRo) o;

        if (entityId != processRo.entityId) return false;
        return name != null ? name.equalsIgnoreCase(processRo.name) : processRo.name == null;
    }

    @Override
    public int hashCode() {
        int result = entityId != null ? entityId.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }


}
