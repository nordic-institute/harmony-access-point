package eu.domibus.core.earchive.eark;

import eu.domibus.core.earchive.BatchEArchiveBasicDTO;
import eu.domibus.core.earchive.BatchEArchiveDTO;
import eu.domibus.core.earchive.EArchiveBatchUserMessage;

import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
public interface EArchivePersistence {

    DomibusEARKSIPResult createEArkSipStructure(BatchEArchiveDTO batchEArchiveDTO, List<EArchiveBatchUserMessage> userMessageEntityIds);

    DomibusEARKSIPResult createEArkSipStructure(BatchEArchiveBasicDTO batchEArchiveDTO, List<EArchiveBatchUserMessage> userMessageEntityIds);

}
