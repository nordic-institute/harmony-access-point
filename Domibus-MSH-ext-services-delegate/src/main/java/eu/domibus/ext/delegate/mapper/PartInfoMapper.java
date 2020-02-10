package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.usermessage.domain.PartInfo;
import eu.domibus.ext.domain.PartInfoDTO;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Ioana Dragusanu (idragusa)
 * @since 4.1
 */
@Mapper(componentModel = "spring")
@DecoratedWith(PartInfoDTODecorator.class)
public interface PartInfoMapper {

    @Mapping(target = "payload", ignore = true)
    PartInfoDTO partInfoToPartInfoDTO(PartInfo partInfo);


}
