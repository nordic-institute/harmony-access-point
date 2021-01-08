package eu.domibus.core.ebms3.mapper.usermessage;

import eu.domibus.api.ebms3.model.Ebms3PartyInfo;
import eu.domibus.api.model.PartyInfo;
import eu.domibus.core.ebms3.mapper.Ebms3CentralMapperConfig;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Mapper(config = Ebms3CentralMapperConfig.class,
        uses = {
                Ebms3PartyFromMapper.class,
                Ebms3PartyToMapper.class,
        })
public interface Ebms3PartyInfoMapper {

    @Mapping(source = "from", target = "from")
    @Mapping(source = "to", target = "to")
    Ebms3PartyInfo partyInfoEntityToEbms3(PartyInfo partyInfo);

    @InheritInverseConfiguration
    PartyInfo partyInfoEbms3ToEntity(Ebms3PartyInfo ebms3PartyInfo);
}
