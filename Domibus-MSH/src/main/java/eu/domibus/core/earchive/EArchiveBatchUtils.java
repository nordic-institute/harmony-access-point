package eu.domibus.core.earchive;

import eu.domibus.core.message.UserMessageLogDao;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.MAX_INCREMENT_NUMBER;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
@Component
public class EArchiveBatchUtils {

    public List<String> getMessageIds(List<EArchiveBatchUserMessage> userMessageDtos) {
        if (CollectionUtils.isEmpty(userMessageDtos)) {
            return new ArrayList<>();
        }
        return userMessageDtos.stream().map(EArchiveBatchUserMessage::getMessageId).collect(Collectors.toList());
    }

    public List<Long> getEntityIds(List<EArchiveBatchUserMessage> userMessageDtos) {
        if (CollectionUtils.isEmpty(userMessageDtos)) {
            return new ArrayList<>();
        }
        return userMessageDtos.stream().map(EArchiveBatchUserMessage::getUserMessageEntityId).collect(Collectors.toList());
    }

    public Long extractDateFromPKUserMessageId(Long pkUserMessage) {
        if (pkUserMessage == null) {
            return null;
        }
        return pkUserMessage / (MAX_INCREMENT_NUMBER + 1);
    }

    Long dateToPKUserMessageId(Long pkUserMessageDate) {
        return pkUserMessageDate == null ? null : pkUserMessageDate * (MAX_INCREMENT_NUMBER + 1);
    }

    public int getLastIndex(List<EArchiveBatchUserMessage> batchUserMessages) {
        if (org.springframework.util.CollectionUtils.isEmpty(batchUserMessages)) {
            return 0;
        }
        return batchUserMessages.size() - 1;
    }

    public String getMessageStartDate(List<EArchiveBatchUserMessage> batchUserMessages, int index) {
        if (org.springframework.util.CollectionUtils.isEmpty(batchUserMessages)) {
            return null;
        }
        return "" + batchUserMessages.get(index).getUserMessageEntityId();
    }

    public Date getBatchMessageDate(UserMessageLogDao userMessageLogDao, String userMessageEntityId) {
        Date messageStartDate = null;
        if (userMessageEntityId != null) {
            messageStartDate = userMessageLogDao.findByEntityId(Long.parseLong(userMessageEntityId)).getReceived();
        }
        return messageStartDate;
    }

}
