package eu.domibus.core.ebms3.mapper.signal;

import eu.domibus.api.ebms3.model.Ebms3SignalMessage;
import eu.domibus.api.model.SignalMessage;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
public interface Ebms3SignalMapper {

    Ebms3SignalMessage signalMessageEntityToEbms3(SignalMessage signalMessage);

    SignalMessage signalMessageEbms3ToEntity(Ebms3SignalMessage ebms3SignalMessage);
}
