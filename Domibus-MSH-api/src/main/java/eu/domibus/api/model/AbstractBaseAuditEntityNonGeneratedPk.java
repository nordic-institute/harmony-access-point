package eu.domibus.api.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@XmlTransient
@MappedSuperclass
public class AbstractBaseAuditEntityNonGeneratedPk extends AbstractNoGeneratedPkEntity {

    @Column(name = "CREATED_BY")
    private String createdBy;

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

}
