package eu.domibus.core.ebms3.receiver.leg;

import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.SignalMessage;
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
@Qualifier("errorSignalConfigurationFactory")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ErrorSignalConfigurationFactory extends AbstractMessageLegConfigurationFactory {

    @Override
    protected LegConfigurationExtractor getConfiguration(SoapMessage soapMessage, Messaging messaging) {
        ErrorSignalLegConfigurationExtractor errorSignalLegConfigurationExtractor = null;
        if (handleError(messaging.getSignalMessage())) {
            errorSignalLegConfigurationExtractor = new ErrorSignalLegConfigurationExtractor(soapMessage, messaging);
        }
        return errorSignalLegConfigurationExtractor;
    }

    protected boolean handleError(SignalMessage signalMessage) {
        if (CollectionUtils.isEmpty(signalMessage.getError())) {
            return false;
        }
        return true;
    }
}
