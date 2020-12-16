package eu.domibus.core.ebms3.mapper.usermessage;

import eu.domibus.api.ebms3.model.Ebms3PartyId;
import eu.domibus.api.model.PartyId;
import eu.domibus.core.ebms3.mapper.Ebms3CentralMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(config = Ebms3CentralMapperConfig.class)
public interface Ebms3PartyIdMapper {

    @Mapping(source = "value", target = "value")
    @Mapping(source = "type", target = "type")
    @BeanMapping(ignoreUnmappedSourceProperties = {"entityId", "creationTime", "modificationTime", "createdBy", "modifiedBy"})
    Ebms3PartyId partyIdEntityToEbms3(PartyId partyId);

    @Mapping(target = "entityId", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "modificationTime", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @InheritInverseConfiguration
    PartyId partyIdEbms3ToEntity(Ebms3PartyId ebms3PartyId);
}
