package eu.domibus.core.ebms3.mapper.usermessage;

import eu.domibus.api.ebms3.model.Ebms3UserMessage;
import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.UserMessage;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
public interface Ebms3UserMessageMapper {

    Ebms3UserMessage userMessageEntityToEbms3(UserMessage userMessage, List<PartInfo> partInfoList);

    UserMessage userMessageEbms3ToEntity(Ebms3UserMessage ebms3UserMessage);

    List<PartInfo> partInfoEbms3ToEntity(Ebms3UserMessage ebms3UserMessage);
}
