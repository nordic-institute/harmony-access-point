package eu.domibus.core.earchive.eark;

import eu.domibus.core.earchive.BatchEArchiveDTO;
import eu.domibus.core.earchive.EArchiveBatchUserMessage;
import org.apache.commons.vfs2.FileObject;

import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
public interface EArchivePersistence {

    FileObject createEArkSipStructure(BatchEArchiveDTO batchEArchiveDTO, List<EArchiveBatchUserMessage> userMessageEntityIds);

}
