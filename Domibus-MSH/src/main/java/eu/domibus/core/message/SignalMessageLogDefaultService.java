package eu.domibus.core.message;

import eu.domibus.api.message.MessageSubtype;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.model.logging.SignalMessageLog;
import eu.domibus.common.model.logging.SignalMessageLogBuilder;
import eu.domibus.ebms3.common.model.Ebms3Constants;
import eu.domibus.ebms3.common.model.SignalMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of Default Service for SignalMessageLog
 *
 * @author Tiago Miguel
 * @since 4.0
 */
@Service
public class SignalMessageLogDefaultService {

    @Autowired
    SignalMessageLogDao signalMessageLogDao;

    private SignalMessageLog createSignalMessageLog(SignalMessage signalMessage, MessageSubtype messageSubtype) {
        // builds the signal message log
        String messageId = signalMessage.getMessageInfo().getMessageId();
        SignalMessageLogBuilder smlBuilder = SignalMessageLogBuilder.create()
                .setMessageId(messageId)
                .setMessageStatus(MessageStatus.RECEIVED)
                .setMshRole(MSHRole.RECEIVING)
                .setNotificationStatus(NotificationStatus.NOT_REQUIRED)
                .setMessageSubtype(messageSubtype)
                .setSignalMessage(signalMessage);

        return smlBuilder.build();
    }


    public SignalMessageLog save(SignalMessage signalMessage, String userMessageService, String userMessageAction) {
        // Sets the subtype

        MessageSubtype messageSubtype = null;
        if (checkTestMessage(userMessageService, userMessageAction)) {
            messageSubtype = MessageSubtype.TEST;
        }
        // Builds the signal message log
        final SignalMessageLog signalMessageLog = createSignalMessageLog(signalMessage, messageSubtype);

        // Saves an entry of the signal message log
//        signalMessageLogDao.create(signalMessageLog);
        return signalMessageLog;
    }

    /**
     * Checks <code>service</code> and <code>action</code> to determine if it's a TEST message
     *
     * @param service Service
     * @param action  Action
     * @return True, if it's a test message and false otherwise
     */
    protected Boolean checkTestMessage(final String service, final String action) {
        return Ebms3Constants.TEST_SERVICE.equalsIgnoreCase(service)
                && Ebms3Constants.TEST_ACTION.equalsIgnoreCase(action);

    }
}
