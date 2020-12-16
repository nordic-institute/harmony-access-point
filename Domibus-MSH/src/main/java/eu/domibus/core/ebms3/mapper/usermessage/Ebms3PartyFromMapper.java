package eu.domibus.core.ebms3.mapper.usermessage;

import eu.domibus.api.ebms3.model.Ebms3From;
import eu.domibus.api.model.From;
import eu.domibus.core.ebms3.mapper.Ebms3CentralMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(config = Ebms3CentralMapperConfig.class,
        uses = {
                Ebms3PartyIdMapper.class,
        })
public interface Ebms3PartyFromMapper {

    @Mapping(source = "partyId", target = "partyId")
    @Mapping(source = "role", target = "role")
    @BeanMapping(ignoreUnmappedSourceProperties = {"firstPartyId"})
    Ebms3From partyFromEntityToEbms3(From from);

    @InheritInverseConfiguration
    From partyFromEbms3ToEntity(Ebms3From ebms3From);
}
