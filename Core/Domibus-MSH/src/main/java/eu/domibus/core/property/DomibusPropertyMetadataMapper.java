package eu.domibus.core.property;

import eu.domibus.api.property.DomibusProperty;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.web.rest.ro.DomibusPropertyRO;
import eu.domibus.web.rest.ro.DomibusPropertyTypeRO;
import eu.domibus.web.rest.ro.PropertyFilterRequestRO;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DomibusPropertyMetadataMapper {

    //cloning mappings
    DomibusPropertyMetadata clonePropertyMetadata(DomibusPropertyMetadata src);

    default DomibusPropertyRO propertyApiToPropertyRO(DomibusProperty entity) {
        DomibusPropertyRO res = propertyMetadataApiToPropertyRO(entity.getMetadata());
        res.setValue(entity.getValue());
        res.setUsedValue(entity.getUsedValue());
        return res;
    }

    @Mapping(target = "usageText", expression = "java( meta.getUsageText() )")
    DomibusPropertyRO propertyMetadataApiToPropertyRO(DomibusPropertyMetadata meta);

    default DomibusPropertyTypeRO domibusPropertyMetadataTypeTOdomibusPropertyTypeRO(DomibusPropertyMetadata.Type type){
        return new DomibusPropertyTypeRO(type.name(), type.getRegularExpression());
    }

    @InheritInverseConfiguration
    DomibusPropertyMetadata propertyMetadataDTOTopropertyMetadata(DomibusPropertyMetadataDTO src);

    DomibusPropertiesFilter domibusPropertyFilterRequestTOdomibusPropertiesFilter(PropertyFilterRequestRO source);

    List<DomibusPropertyRO> domibusPropertyListToDomibusPropertyROList(List<DomibusProperty> domibusPropertyList);

    List<DomibusPropertyTypeRO> domibusPropertyMetadataTypeListToDomibusPropertyTypeROList(List<DomibusPropertyMetadata.Type> domibusPropertyMetadataTypeList);
}
