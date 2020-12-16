package eu.domibus.core.ebms3.mapper.usermessage;

import eu.domibus.api.ebms3.model.Ebms3CollaborationInfo;
import eu.domibus.api.model.CollaborationInfo;
import eu.domibus.core.ebms3.mapper.Ebms3CentralMapperConfig;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(config = Ebms3CentralMapperConfig.class,
        uses = {
                Ebms3AgreementRefMapper.class,
                Ebms3ServiceMapper.class,
        })
public interface Ebms3CollaborationInfoMapper {

    @Mapping(source = "conversationId", target = "conversationId")
    @Mapping(source = "action", target = "action")
    @Mapping(source = "agreementRef", target = "agreementRef")
    @Mapping(source = "service", target = "service")
    Ebms3CollaborationInfo collaborationInfoEntityToEbms3(CollaborationInfo collaborationInfo);

    @InheritInverseConfiguration
    CollaborationInfo collaborationInfoEbms3ToEntity(Ebms3CollaborationInfo ebms3CollaborationInfo);
}
