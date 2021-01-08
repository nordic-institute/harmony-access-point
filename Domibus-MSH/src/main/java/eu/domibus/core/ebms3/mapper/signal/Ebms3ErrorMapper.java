package eu.domibus.core.ebms3.mapper.signal;

import eu.domibus.api.ebms3.model.Ebms3Error;
import eu.domibus.api.model.Error;
import eu.domibus.core.ebms3.mapper.Ebms3CentralMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Mapper(config = Ebms3CentralMapperConfig.class)
public interface Ebms3ErrorMapper {

    @Mapping(source = "description", target = "description")
    @Mapping(source = "errorDetail", target = "errorDetail")
    @Mapping(source = "category", target = "category")
    @Mapping(source = "refToMessageInError", target = "refToMessageInError")
    @Mapping(source = "errorCode", target = "errorCode")
    @Mapping(source = "origin", target = "origin")
    @Mapping(source = "severity", target = "severity")
    @Mapping(source = "shortDescription", target = "shortDescription")
    @BeanMapping(ignoreUnmappedSourceProperties = {"entityId", "creationTime", "modificationTime", "createdBy", "modifiedBy"})
    Ebms3Error errorEntityToEbms3(Error error);

    @Mapping(target = "entityId", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "modificationTime", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @InheritInverseConfiguration
    Error errorEbms3ToEntity(Ebms3Error ebms3Error);
}
