package eu.domibus.core.message.converter;

import eu.domibus.api.model.Messaging;

/**
 * Created by musatmi on 11/05/2017.
 */
public interface MessageConverterService {
    byte[] getAsByteArray(Messaging message);
}
