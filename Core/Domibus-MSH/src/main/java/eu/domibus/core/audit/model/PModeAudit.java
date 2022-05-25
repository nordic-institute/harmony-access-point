package eu.domibus.core.audit.model;

import eu.domibus.api.audit.envers.RevisionLogicalName;
import eu.domibus.core.audit.envers.ModificationType;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Date;


/**
 * Entity used to track actions on the Pmode download actions
 *
 * @author Joze Rihtarsic
 * @since 4.0
 * <p>
 */
@Entity
@DiscriminatorValue("Pmode")
@RevisionLogicalName("Pmode")
public class PModeAudit extends AbstractGenericAudit {


    public PModeAudit() {
    }

    public PModeAudit(
            final long id,
            final String userName,
            final Date revisionDate,
            final ModificationType modificationType) {
        super(String.valueOf(id), userName, revisionDate, modificationType);
    }
}
