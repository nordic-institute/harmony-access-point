package eu.domibus.core.message.signal;

import eu.domibus.api.model.*;
import eu.domibus.api.message.SignalMessageLogService;
import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.core.message.MessageStatusDao;
import eu.domibus.core.message.MshRoleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of Default Service for SignalMessageLog
 * @author Tiago Miguel
 * @since 4.0
 */
@Service
public class SignalMessageLogDefaultService implements SignalMessageLogService {

    @Autowired
    SignalMessageLogDao signalMessageLogDao;

    @Autowired
    protected MessageStatusDao messageStatusDao;

    @Autowired
    protected MshRoleDao mshRoleDao;

    private SignalMessageLog createSignalMessageLog(SignalMessage signalMessage) {
        MessageStatusEntity messageStatus = messageStatusDao.findOrCreate(MessageStatus.RECEIVED);
        MSHRoleEntity role = mshRoleDao.findOrCreate(MSHRole.RECEIVING);

        // builds the signal message log
        SignalMessageLogBuilder smlBuilder = SignalMessageLogBuilder.create()
                .setSignalMessage(signalMessage)
                .setMessageStatus(messageStatus)
                .setMshRole(role);

        return smlBuilder.build();
    }


    @Override
    public void save(SignalMessage signalMessage, String userMessageService, String userMessageAction) {
        final Boolean testMessage = checkTestMessage(userMessageService, userMessageAction);

        // Builds the signal message log
        final SignalMessageLog signalMessageLog = createSignalMessageLog(signalMessage);
        // Saves an entry of the signal message log
        signalMessageLogDao.create(signalMessageLog);
    }

    /**
     * Checks <code>service</code> and <code>action</code> to determine if it's a TEST message
     * @param service Service
     * @param action Action
     * @return True, if it's a test message and false otherwise
     */
    protected Boolean checkTestMessage(final String service, final String action) {
        return Ebms3Constants.TEST_SERVICE.equalsIgnoreCase(service)
                && Ebms3Constants.TEST_ACTION.equalsIgnoreCase(action);

    }
}
