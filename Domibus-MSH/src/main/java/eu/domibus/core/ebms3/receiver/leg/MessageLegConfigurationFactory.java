package eu.domibus.core.ebms3.receiver.leg;

import eu.domibus.api.model.Messaging;
import org.apache.cxf.binding.soap.SoapMessage;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public interface MessageLegConfigurationFactory {

    LegConfigurationExtractor extractMessageConfiguration(final SoapMessage soapMessage, final Messaging messaging);


}
