package eu.domibus.core.ebms3.receiver.leg;

import eu.domibus.common.ErrorCode;
import eu.domibus.api.model.Messaging;
import eu.domibus.api.model.SignalMessage;
import org.apache.commons.collections.CollectionUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

@Component
@Qualifier("serverInReceiptLegConfigurationFactory")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ServerInReceiptLegConfigurationFactory extends AbstractMessageLegConfigurationFactory {

    @Override
    protected LegConfigurationExtractor getConfiguration(SoapMessage soapMessage, Messaging messaging) {
        ReceiptLegConfigurationExtractor receiptLegConfigurationExtractor = null;
        if (messaging.getSignalMessage().getReceipt() != null || handleError(messaging.getSignalMessage())) {
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
