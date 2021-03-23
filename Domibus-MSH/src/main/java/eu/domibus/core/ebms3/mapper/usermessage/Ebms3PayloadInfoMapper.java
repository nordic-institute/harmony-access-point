package eu.domibus.core.ebms3.mapper.usermessage;

import eu.domibus.api.ebms3.model.Ebms3PayloadInfo;
import eu.domibus.api.model.PartInfo;
import eu.domibus.core.ebms3.mapper.Ebms3CentralMapperConfig;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
/*@Mapper(config = Ebms3CentralMapperConfig.class,
        uses = {
                Ebms3PartInfoMapper.class,
        })*/
public interface Ebms3PayloadInfoMapper {

    @Mapping(source = "partInfo", target = "partInfo")
    Ebms3PayloadInfo payloadInfoEntityToEbms3(List<PartInfo> partInfoList);

    @InheritInverseConfiguration
    List<PartInfo> payloadInfoEbms3ToEntity(Ebms3PayloadInfo ebms3PayloadInfo);
}
