package eu.domibus.core.message.converter;

import eu.domibus.api.model.UserMessage;

/**
 * Created by musatmi on 11/05/2017.
 */
public interface MessageConverterService {
    byte[] getAsByteArray(UserMessage userMessage);
}
