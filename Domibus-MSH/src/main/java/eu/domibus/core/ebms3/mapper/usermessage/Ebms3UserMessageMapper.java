package eu.domibus.core.ebms3.mapper.usermessage;

import eu.domibus.api.ebms3.model.Ebms3UserMessage;
import eu.domibus.api.model.UserMessage;
import eu.domibus.core.ebms3.mapper.Ebms3CentralMapperConfig;
import eu.domibus.core.ebms3.mapper.signal.Ebms3MessageInfoMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Mapper(config = Ebms3CentralMapperConfig.class,
        uses = {
                Ebms3MessageInfoMapper.class,
                Ebms3PartyInfoMapper.class,
                Ebms3CollaborationInfoMapper.class,
                Ebms3MessagePropertiesMapper.class,
                Ebms3PayloadInfoMapper.class,
        })
public interface Ebms3UserMessageMapper {

    @Mapping(source = "messageInfo", target = "messageInfo")
    @Mapping(source = "partyInfo", target = "partyInfo")
    @Mapping(source = "collaborationInfo", target = "collaborationInfo")
    @Mapping(source = "messageProperties", target = "messageProperties")
    @Mapping(source = "payloadInfo", target = "payloadInfo")
    @Mapping(source = "mpc", target = "mpc")
    @BeanMapping(ignoreUnmappedSourceProperties = {
            "splitAndJoin",
            "messageFragment",
            "rawEnvelopeLog",
            "entityId",
            "creationTime",
            "modificationTime",
            "createdBy",
            "modifiedBy",
            "fromFirstPartyId",
            "toFirstPartyId",
            "userMessageFragment",
            "sourceMessage",
            "payloadOnFileSystem",
    })
    Ebms3UserMessage userMessageEntityToEbms3(UserMessage userMessage);

    @Mapping(target = "entityId", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "modificationTime", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "messageFragment", ignore = true)
    @Mapping(target = "splitAndJoin", ignore = true)
    @InheritInverseConfiguration
    UserMessage userMessageEbms3ToEntity(Ebms3UserMessage ebms3UserMessage);
}
