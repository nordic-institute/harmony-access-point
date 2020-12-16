package eu.domibus.core.ebms3.mapper.usermessage;

import eu.domibus.api.ebms3.model.Ebms3PartProperties;
import eu.domibus.api.model.PartProperties;
import eu.domibus.core.ebms3.mapper.Ebms3CentralMapperConfig;
import eu.domibus.core.ebms3.mapper.signal.Ebms3DescriptionMapper;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(config = Ebms3CentralMapperConfig.class,
        uses = {
                Ebms3DescriptionMapper.class,
                Ebms3PropertyMapper.class,
        })
public interface Ebms3PartPropertiesMapper {

    @Mapping(source = "property", target = "property")
    Ebms3PartProperties partPropertiesEntityToEbms3(PartProperties partProperties);

    @InheritInverseConfiguration
    PartProperties partPropertiesEbms3ToEntity(Ebms3PartProperties ebms3PartProperties);
}
