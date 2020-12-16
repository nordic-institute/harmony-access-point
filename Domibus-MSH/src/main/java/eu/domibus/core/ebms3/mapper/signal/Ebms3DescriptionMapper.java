package eu.domibus.core.ebms3.mapper.signal;

import eu.domibus.api.ebms3.model.Ebms3Description;
import eu.domibus.api.model.Description;
import eu.domibus.core.ebms3.mapper.Ebms3CentralMapperConfig;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(config = Ebms3CentralMapperConfig.class)
public interface Ebms3DescriptionMapper {

    @Mapping(source = "value", target = "value")
    @Mapping(source = "lang", target = "lang")
    Ebms3Description descriptionEntityToEbms3(Description description);

    @InheritInverseConfiguration
    Description descriptionEbms3ToEntity(Ebms3Description ebms3Description);
}
