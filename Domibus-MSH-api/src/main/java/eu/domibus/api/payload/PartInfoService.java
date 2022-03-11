package eu.domibus.api.payload;

import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.UserMessage;

import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
public interface PartInfoService {

    void create(PartInfo partInfo, UserMessage userMessage);

    List<PartInfo> findPartInfo(UserMessage userMessage);

    List<PartInfo> findPartInfo(long entityId);

    PartInfo findPartInfo(String messageId, String cid);

    PartInfo findPartInfo(Long messageEntityId, String cid);

    void clearPayloadData(long entityId);

    void clearFileSystemPayloads(List<PartInfo> partInfos);

    void deletePayloadFiles(List<String> filenames);

    List<String> findFileSystemPayloadFilenames(List<String> userMessageEntityIds);

    boolean scheduleSourceMessagePayloads(List<PartInfo> partInfos);

    void loadBinaryData(PartInfo partInfo);
}
