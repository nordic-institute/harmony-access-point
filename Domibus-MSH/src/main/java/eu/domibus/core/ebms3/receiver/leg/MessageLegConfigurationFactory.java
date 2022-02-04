package eu.domibus.core.ebms3.receiver.leg;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import org.apache.cxf.binding.soap.SoapMessage;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public interface MessageLegConfigurationFactory {

    LegConfigurationExtractor extractMessageConfiguration(final SoapMessage soapMessage, final Ebms3Messaging messaging);


}
