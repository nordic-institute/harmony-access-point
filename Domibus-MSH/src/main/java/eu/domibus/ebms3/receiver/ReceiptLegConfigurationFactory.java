package eu.domibus.ebms3.receiver;

import eu.domibus.common.ErrorCode;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.SignalMessage;
import org.apache.commons.collections.CollectionUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

@Component
@Qualifier("pullRequestLegConfigurationFactory")
public class ReceiptLegConfigurationFactory extends AbstractMessageLegConfigurationFactory {

    @Override
    protected LegConfigurationExtractor getConfiguration(SoapMessage soapMessage, Messaging messaging) {
        ReceiptLegConfigurationExtractor receiptLegConfigurationExtractor = null;
        if (messaging.getSignalMessage().getReceipt() != null) {
            receiptLegConfigurationExtractor = new ReceiptLegConfigurationExtractor(soapMessage, messaging);
        }
        return receiptLegConfigurationExtractor;
    }

    protected boolean handleError(SignalMessage signalMessage) {
        if (CollectionUtils.isEmpty(signalMessage.getError())) {
            return false;
        }
        return !signalMessage.getError().stream().anyMatch(candidate -> candidate.getErrorCode().equals(ErrorCode.EbMS3ErrorCode.EBMS_0006.getCode().getErrorCode().getErrorCodeName()));
    }
}
