package eu.domibus.core.party;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class ProcessRo {

    private long entityId;

    private String name;

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
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
        int result = (int)entityId;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }


}
