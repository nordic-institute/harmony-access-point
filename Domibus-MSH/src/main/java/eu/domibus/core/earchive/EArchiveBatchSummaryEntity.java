package eu.domibus.core.earchive;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * ArchiveBatchEntity with only the lightweight fields (does not contain CLOB)
 */
@Entity
@Table(name = "TB_EARCHIVE_BATCH")
public class EArchiveBatchSummaryEntity extends EArchiveBatchBaseEntity {
}
