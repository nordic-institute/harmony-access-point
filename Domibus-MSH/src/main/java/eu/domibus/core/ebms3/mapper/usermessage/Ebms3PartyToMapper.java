package eu.domibus.core.ebms3.mapper.usermessage;

import eu.domibus.api.ebms3.model.Ebms3To;
import eu.domibus.api.model.To;
import eu.domibus.core.ebms3.mapper.Ebms3CentralMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(config = Ebms3CentralMapperConfig.class,
        uses = {
                Ebms3PartyIdMapper.class,
        })
public interface Ebms3PartyToMapper {

    @Mapping(source = "partyId", target = "partyId")
    @Mapping(source = "role", target = "role")
    @BeanMapping(ignoreUnmappedSourceProperties = {"firstPartyId"})
    Ebms3To partyToEntityToEbms3(To from);

    @InheritInverseConfiguration
    To partyToEbms3ToEntity(Ebms3To from);
}
