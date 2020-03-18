package eu.domibus.core.audit.model;

import eu.domibus.core.audit.envers.ModificationType;
import eu.domibus.core.audit.envers.RevisionLogicalName;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Date;


/**
 * @author Joze Rihtarsic
 * @since 4.0
 *
 * Entity used to track actions on the Pmode download actions
 */
@Entity
@DiscriminatorValue("Pmode")
@RevisionLogicalName("Pmode")
public class PModeAudit extends AbstractGenericAudit {


    public PModeAudit() {
    }

    public PModeAudit(
            final String id,
            final String userName,
            final Date revisionDate,
            final ModificationType modificationType) {
        super(id, userName, revisionDate, modificationType);
    }
}
