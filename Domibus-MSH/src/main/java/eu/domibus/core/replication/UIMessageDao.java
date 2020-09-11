package eu.domibus.core.replication;

import eu.domibus.core.message.UserMessageLog;

import java.util.List;
import java.util.Map;

/**
 * @author Catalin Enache
 * @since 4.0
 */
public interface UIMessageDao {

    UIMessageEntity findUIMessageByMessageId(String messageId);

    long countEntries(Map<String, Object> filters);

    List<UIMessageEntity> findPaged(int from, int max, String column, boolean asc, Map<String, Object> filters);

    void saveOrUpdate(UIMessageEntity uiMessageEntity);

    boolean updateMessage(UserMessageLog userMessageLog, long lastModified);

    int deleteUIMessagesByMessageIds(List<String> messageIds);
}
