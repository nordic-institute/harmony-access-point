package eu.domibus;

import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.plugin.handler.DatabaseMessageHandler;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.Submission;
import eu.domibus.test.common.SubmissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class ITTestsService {

    @Autowired
    protected UserMessageLogDao userMessageLogDao;
    @Autowired
    protected SubmissionUtil submissionUtil;

    @Autowired
    protected DatabaseMessageHandler databaseMessageHandler;

    @Transactional
    public String sendMessageToDelete(MessageStatus endStatus) throws MessagingProcessingException {

        Submission submission = submissionUtil.createSubmission();
        final String messageId = databaseMessageHandler.submit(submission, "mybackend");

        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        userMessageLogDao.setMessageStatus(userMessageLog, endStatus);

        return messageId;
    }
}
