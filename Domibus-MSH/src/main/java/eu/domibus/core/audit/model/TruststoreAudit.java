package eu.domibus.core.audit.model;

import eu.domibus.core.audit.envers.ModificationType;
import eu.domibus.core.audit.envers.RevisionLogicalName;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Date;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Entity
@DiscriminatorValue("Truststore")
@RevisionLogicalName("Truststore")
public class TruststoreAudit extends AbstractGenericAudit {

    public TruststoreAudit() {
    }

    public TruststoreAudit(
            final String userName,
            final Date revisionDate,
            final ModificationType modificationType) {
        super("truststore", userName, revisionDate, modificationType);
    }
}
