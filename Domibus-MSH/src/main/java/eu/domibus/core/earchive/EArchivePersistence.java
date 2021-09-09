package eu.domibus.core.earchive;

import org.apache.commons.vfs2.FileObject;

/**
 * @author François Gautier
 * @since 5.0
 */
public interface EArchivePersistence {

    FileObject createEArkSipStructure(BatchEArchiveDTO batchEArchiveDTO);

}
