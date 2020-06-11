package eu.domibus.core.ebms3.sender;

import eu.domibus.api.exceptions.DomibusDateTimeException;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.error.ErrorLogDao;
import eu.domibus.core.error.ErrorLogEntry;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.nonrepudiation.NonRepudiationService;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLogDefaultService;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.ebms3.common.model.Error;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * @author Christian Koch, Stefan Mueller, Federico Martini
 * @author Cosmin Baciu
 */
@Service
public class ResponseHandler {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(ResponseHandler.class);

    private final SignalMessageLogDefaultService signalMessageLogDefaultService;
    private final UIReplicationSignalService uiReplicationSignalService;
    private final NonRepudiationService nonRepudiationService;
    private final SignalMessageDao signalMessageDao;
    protected final MessageUtil messageUtil;
    private final MessagingDao messagingDao;
    private final ErrorLogDao errorLogDao;

    public ResponseHandler(SignalMessageLogDefaultService signalMessageLogDefaultService,
                           UIReplicationSignalService uiReplicationSignalService,
                           NonRepudiationService nonRepudiationService,
                           SignalMessageDao signalMessageDao,
                           MessageUtil messageUtil,
                           MessagingDao messagingDao,
                           ErrorLogDao errorLogDao) {
        this.signalMessageLogDefaultService = signalMessageLogDefaultService;
        this.uiReplicationSignalService = uiReplicationSignalService;
        this.nonRepudiationService = nonRepudiationService;
        this.signalMessageDao = signalMessageDao;
        this.messageUtil = messageUtil;
        this.messagingDao = messagingDao;
        this.errorLogDao = errorLogDao;
    }

    public ResponseResult verifyResponse(final SOAPMessage response, String messageId) throws EbMS3Exception {
        LOGGER.debug("Verifying response");

        ResponseResult result = new ResponseResult();

        final Messaging messaging;
        try {
            messaging = messageUtil.getMessagingWithDom(response);
            result.setResponseMessaging(messaging);
        } catch (SOAPException | DomibusDateTimeException ex) {
            throw EbMS3ExceptionBuilder
                    .getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0004)
                    .errorDetail("Problem occurred during marshalling")
                    .refToMessageId(messageId)
                    .mshRole(MSHRole.SENDING)
                    .cause(ex)
                    .build();
        }

        final SignalMessage signalMessage = messaging.getSignalMessage();
        final ResponseStatus responseStatus = getResponseStatus(signalMessage);
        result.setResponseStatus(responseStatus);

        return result;
    }

    public void saveResponse(final SOAPMessage response, final Messaging sentMessage, final Messaging messagingResponse) {
        final SignalMessage signalMessage = messagingResponse.getSignalMessage();
        nonRepudiationService.saveResponse(response, signalMessage);

        // Stores the signal message
        signalMessageDao.create(signalMessage);

        sentMessage.setSignalMessage(signalMessage);
        messagingDao.update(sentMessage);

        // Builds the signal message log
        // Updating the reference to the signal message
        String userMessageService = sentMessage.getUserMessage().getCollaborationInfo().getService().getValue();
        String userMessageAction = sentMessage.getUserMessage().getCollaborationInfo().getAction();

        signalMessageLogDefaultService.save(signalMessage.getMessageInfo().getMessageId(), userMessageService, userMessageAction);

        createWarningEntries(signalMessage);

        //UI replication
        uiReplicationSignalService.signalMessageReceived(signalMessage.getMessageInfo().getMessageId());
    }

    protected void createWarningEntries(SignalMessage signalMessage) {
        if (signalMessage.getError() == null || signalMessage.getError().isEmpty()) {
            LOGGER.debug("No warning entries to create");
            return;
        }

        LOGGER.debug("Creating warning entries");

        for (final Error error : signalMessage.getError()) {
            if (ErrorCode.SEVERITY_WARNING.equalsIgnoreCase(error.getSeverity())) {
                final String errorCode = error.getErrorCode();
                final String errorDetail = error.getErrorDetail();
                final String refToMessageInError = error.getRefToMessageInError();

                LOGGER.warn("Creating warning error with error code [{}], error detail [{}] and refToMessageInError [{}]", errorCode, errorDetail, refToMessageInError);

                EbMS3Exception ebMS3Ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.findErrorCodeBy(errorCode), errorDetail, refToMessageInError, null);
                final ErrorLogEntry errorLogEntry = new ErrorLogEntry(ebMS3Ex);
                this.errorLogDao.create(errorLogEntry);
            }
        }
    }

    protected ResponseStatus getResponseStatus(SignalMessage signalMessage) throws EbMS3Exception {
        LOGGER.debug("Getting response status");

        // Checks if the signal message is Ok
        if (signalMessage.getError() == null || signalMessage.getError().isEmpty()) {
            LOGGER.debug("Response message contains no errors");
            return ResponseStatus.OK;
        }

        for (final Error error : signalMessage.getError()) {
            if (ErrorCode.SEVERITY_FAILURE.equalsIgnoreCase(error.getSeverity())) {
                EbMS3Exception ebMS3Ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.findErrorCodeBy(error.getErrorCode()), error.getErrorDetail(), error.getRefToMessageInError(), null);
                ebMS3Ex.setMshRole(MSHRole.SENDING);
                throw ebMS3Ex;
            }
        }

        return ResponseStatus.WARNING;
    }


    public enum ResponseStatus {
        OK, WARNING, UNMARSHALL_ERROR
    }

}
