package eu.domibus.core.replication;

import eu.domibus.core.message.MessageLogDaoBase;
import eu.domibus.core.message.UserMessageLog;

import java.util.List;
import java.util.Map;

/**
 * @author Catalin Enache
 * @since 4.0
 */
public interface UIMessageDao extends MessageLogDaoBase {

    UIMessageEntity findUIMessageByMessageId(String messageId);

    List<UIMessageEntity> findPaged(int from, int max, String column, boolean asc, Map<String, Object> filters);

    void saveOrUpdate(UIMessageEntity uiMessageEntity);

    boolean updateMessage(UserMessageLog userMessageLog, long lastModified);

    int deleteUIMessagesByMessageIds(List<String> messageIds);

}
