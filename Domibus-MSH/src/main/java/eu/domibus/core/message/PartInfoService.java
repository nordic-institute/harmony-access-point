package eu.domibus.core.message;

import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.UserMessage;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;

import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
public interface PartInfoService {

    void create(PartInfo partInfo, UserMessage userMessage);

    List<PartInfo> findPartInfo(UserMessage userMessage);

    List<PartInfo> findPartInfo(long entityId);

    void clearPayloadData(long entityId);

    void clearFileSystemPayloads(List<PartInfo> partInfos);

    void deletePayloadFiles(List<String> filenames);

    List<String> findFileSystemPayloadFilenames(List<String> userMessageEntityIds);

    boolean scheduleSourceMessagePayloads(List<PartInfo> partInfos);

    void validatePayloadSizeBeforeSchedulingSave(LegConfiguration legConfiguration, List<PartInfo> partInfos);

    void checkPartInfoCharset(UserMessage userMessage, List<PartInfo> partInfoList) throws EbMS3Exception;
}
