package eu.domibus.ebms3.common.model;

import java.io.Serializable;

/**
 * Base type for entity
 * <p>
 * For convenience we are using the same base entity as domibus core
 */
public interface DomibusBaseEntity extends Serializable {

    long getEntityId();

    default String getCreatedBy() {
        return null;
    }

    default void setCreatedBy(String createdBy) {}

}
