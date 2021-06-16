package eu.domibus.core.message.converter;

import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.UserMessage;

import java.util.List;

/**
 * Created by musatmi on 11/05/2017.
 */
public interface MessageConverterService {

    byte[] getAsByteArray(UserMessage userMessage, List<PartInfo> partInfoList);
}
