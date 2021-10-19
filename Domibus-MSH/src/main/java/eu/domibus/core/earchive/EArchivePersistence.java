package eu.domibus.core.earchive;

import eu.domibus.api.model.UserMessageDTO;
import org.apache.commons.vfs2.FileObject;

import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
public interface EArchivePersistence {

    FileObject createEArkSipStructure(BatchEArchiveDTO batchEArchiveDTO, List<UserMessageDTO> userMessageEntityIds);

}
