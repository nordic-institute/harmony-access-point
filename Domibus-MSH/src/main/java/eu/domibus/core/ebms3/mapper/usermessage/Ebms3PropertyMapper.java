package eu.domibus.core.ebms3.mapper.usermessage;

import eu.domibus.api.ebms3.model.Ebms3Property;
import eu.domibus.api.model.Property;
import eu.domibus.core.ebms3.mapper.Ebms3CentralMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(config = Ebms3CentralMapperConfig.class)
public interface Ebms3PropertyMapper {

    @Mapping(source = "value", target = "value")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "type", target = "type")
    @BeanMapping(ignoreUnmappedSourceProperties = {"entityId", "creationTime", "modificationTime", "createdBy", "modifiedBy"})
    Ebms3Property propertyEntityToEbms3(Property property);

    @Mapping(target = "entityId", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "modificationTime", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @InheritInverseConfiguration
    Property propertyEbms3ToEntity(Ebms3Property ebms3Property);
}
