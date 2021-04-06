package eu.domibus.core.ebms3.mapper.usermessage;

import eu.domibus.api.ebms3.model.Ebms3Service;
import eu.domibus.api.model.ServiceEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapping;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
//@Mapper(config = Ebms3CentralMapperConfig.class)
public interface Ebms3ServiceMapper {

    @Mapping(source = "value", target = "value")
    @Mapping(source = "type", target = "type")
    Ebms3Service serviceEntityToEbms3(ServiceEntity service);

    @InheritInverseConfiguration
    ServiceEntity serviceEbms3ToEntity(Ebms3Service ebms3Service);
}
