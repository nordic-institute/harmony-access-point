package eu.domibus.api.message.validation;



import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.UserMessage;

import java.util.List;

public interface UserMessageValidatorSpiService {

    void validate(UserMessage userMessage, List<PartInfo> partInfos);
}
