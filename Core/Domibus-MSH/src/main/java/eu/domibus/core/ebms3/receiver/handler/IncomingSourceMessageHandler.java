package eu.domibus.core.ebms3.receiver.handler;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.UserMessage;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.message.UserMessagePayloadService;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.List;

/**
 * Handles the incoming source message for SplitAndJoin mechanism
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class IncomingSourceMessageHandler extends AbstractIncomingMessageHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(IncomingSourceMessageHandler.class);

    @Autowired
    protected PayloadFileStorageProvider storageProvider;

    @Autowired
    protected UserMessagePayloadService userMessagePayloadService;

    @Override
    protected SOAPMessage processMessage(LegConfiguration legConfiguration, String pmodeKey, SOAPMessage request, Ebms3Messaging ebms3Messaging, boolean testMessage) throws EbMS3Exception, TransformerException, IOException, JAXBException, SOAPException {
        LOG.debug("Processing SourceMessage");

        if (storageProvider.isPayloadsPersistenceInDatabaseConfigured()) {
            LOG.error("SplitAndJoin feature needs payload storage on the file system");
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0002)
                    .message("SplitAndJoin feature needs payload storage on the file system")
                    .refToMessageId(ebms3Messaging.getUserMessage().getMessageInfo().getMessageId())
                    .mshRole(MSHRole.RECEIVING)
                    .build();
        }

        List<PartInfo> partInfoList = userMessagePayloadService.handlePayloads(request, ebms3Messaging, null);
        UserMessage userMessage = ebms3Converter.convertFromEbms3(ebms3Messaging.getUserMessage());
        return userMessageHandlerService.handleNewSourceUserMessage(legConfiguration, pmodeKey, request, userMessage, partInfoList, testMessage);
    }
}
