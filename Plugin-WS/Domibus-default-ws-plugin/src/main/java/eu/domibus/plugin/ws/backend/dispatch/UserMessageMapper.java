package eu.domibus.plugin.ws.backend.dispatch;

import org.mapstruct.Mapper;

/**
 * @author Francois Gautier
 * @since 5.0
 */
@Mapper(componentModel = "spring")
public interface UserMessageMapper {

    eu.domibus.webservice.backend.generated.UserMessage userMessageDTOToUserMessage(eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage userMessageDTO);

}
