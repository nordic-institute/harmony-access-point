package eu.domibus.api.model;

import java.io.Serializable;


public interface DomibusBaseEntity extends Serializable {

    long getEntityId();

    default String getCreatedBy() {
        return null;
    }

    default void setCreatedBy(String createdBy) {}

}
