package eu.domibus.core.audit.model;

import eu.domibus.core.audit.envers.ModificationType;
import eu.domibus.core.audit.envers.RevisionLogicalName;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Date;


/**
 * @author Soumya Chandran
 * @since 4.2
 * <p>
 * Entity used to track actions on the Pmode Archive download actions
 */
@Entity
@DiscriminatorValue("Pmode Archive")
@RevisionLogicalName("Pmode Archive")
public class PModeArchiveAudit extends AbstractGenericAudit {


    public PModeArchiveAudit() {
    }

    public PModeArchiveAudit(
            final String id,
            final String userName,
            final Date revisionDate,
            final ModificationType modificationType) {
        super(id, userName, revisionDate, modificationType);
    }
}
