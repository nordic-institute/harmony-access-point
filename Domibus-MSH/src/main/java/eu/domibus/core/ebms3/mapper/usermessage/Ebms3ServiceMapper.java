package eu.domibus.core.ebms3.mapper.usermessage;

import eu.domibus.api.ebms3.model.Ebms3Service;
import eu.domibus.api.model.Service;
import eu.domibus.core.ebms3.mapper.Ebms3CentralMapperConfig;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(config = Ebms3CentralMapperConfig.class)
public interface Ebms3ServiceMapper {

    @Mapping(source = "value", target = "value")
    @Mapping(source = "type", target = "type")
    Ebms3Service serviceEntityToEbms3(Service service);

    @InheritInverseConfiguration
    Service serviceEbms3ToEntity(Ebms3Service ebms3Service);
}
