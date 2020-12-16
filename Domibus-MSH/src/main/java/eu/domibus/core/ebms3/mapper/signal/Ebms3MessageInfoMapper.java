package eu.domibus.core.ebms3.mapper.signal;

import eu.domibus.api.ebms3.model.Ebms3MessageInfo;
import eu.domibus.api.model.MessageInfo;
import eu.domibus.core.ebms3.mapper.Ebms3CentralMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = Ebms3CentralMapperConfig.class)
public interface Ebms3MessageInfoMapper {

    @Mapping(source = "timestamp", target = "timestamp")
    @Mapping(source = "messageId", target = "messageId")
    @Mapping(source = "refToMessageId", target = "refToMessageId")
    @BeanMapping(ignoreUnmappedSourceProperties = {"entityId", "creationTime", "modificationTime", "createdBy", "modifiedBy"})
    Ebms3MessageInfo messageInfoEntityToEbms3(MessageInfo messageInfo);

    @Mapping(target = "entityId", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "modificationTime", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @InheritInverseConfiguration
    MessageInfo messageInfoEbms3ToEntity(Ebms3MessageInfo ebms3MessageInfo);
}
