package eu.domibus.core.earchive;

import java.nio.file.Path;

/**
 * @author François Gautier
 * @since 5.0
 */
public interface EArchivePersistence {

    Path createEArkSipStructure(BatchEArchiveDTO batchEArchiveDTO);

}
