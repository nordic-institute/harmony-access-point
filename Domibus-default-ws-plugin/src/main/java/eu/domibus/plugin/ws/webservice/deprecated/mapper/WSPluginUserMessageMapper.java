package eu.domibus.plugin.ws.webservice.deprecated.mapper;

import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;


/**
 * @author François Gautier
 * @since 5.0
 * @deprecated since 5.0 no substitute
 */
@Mapper(componentModel = "spring",
        unmappedSourcePolicy = ReportingPolicy.ERROR,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
@Deprecated
public interface WSPluginUserMessageMapper {
    eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage userMessageEntityTo(UserMessage userMessage);
    UserMessage userMessageToEntity(eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage ebms3UserMessage);
}
