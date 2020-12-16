package eu.domibus.core.ebms3.mapper.usermessage;

import eu.domibus.api.ebms3.model.Ebms3PartInfo;
import eu.domibus.api.model.PartInfo;
import eu.domibus.core.ebms3.mapper.Ebms3CentralMapperConfig;
import eu.domibus.core.ebms3.mapper.signal.Ebms3DescriptionMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(config = Ebms3CentralMapperConfig.class,
        uses = {
                Ebms3DescriptionMapper.class,
                Ebms3PartPropertiesMapper.class
        })
public interface Ebms3PartInfoMapper {

    @Mapping(source = "description", target = "description")
    @Mapping(source = "partProperties", target = "partProperties")
    @Mapping(source = "href", target = "href")
    @Mapping(source = "fileName", target = "fileName")
    @Mapping(source = "inBody", target = "inBody")
    @Mapping(source = "payloadDatahandler", target = "payloadDatahandler")
    @Mapping(source = "mime", target = "mime")
    @Mapping(source = "length", target = "length")
    @Mapping(source = "partOrder", target = "partOrder")
    @Mapping(source = "encrypted", target = "encrypted")
    @BeanMapping(ignoreUnmappedSourceProperties = {"schema", "entityId", "creationTime", "modificationTime", "createdBy", "modifiedBy"})
    Ebms3PartInfo partInfoEntityToEbms3(PartInfo partInfo);

    @Mapping(target = "entityId", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "modificationTime", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @InheritInverseConfiguration
    PartInfo partInfoEbms3ToEntity(Ebms3PartInfo ebms3PartInfo);
}
